/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2019
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package com.lukeneedham.minecartcoupling.common.carts;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.MapMaker;
import com.lukeneedham.minecartcoupling.common.carts.coupling.CouplingsDao;
import com.lukeneedham.minecartcoupling.common.util.CartTools;
import com.lukeneedham.minecartcoupling.common.util.Game;
import com.lukeneedham.minecartcoupling.common.util.NBTPlugin;
import com.lukeneedham.minecartcoupling.common.util.Streams;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public final class Train implements Iterable<EntityMinecart> {
    public static final String TRAIN_NBT = "rcTrain";

    private final UUID uuid;
    private final LinkedList<UUID> carts = new LinkedList<>();
    private final List<UUID> safeCarts = Collections.unmodifiableList(carts);
    private final Set<UUID> locks = new HashSet<>();
    private @Nullable
    World world;
    private boolean dirty = true;
    private boolean isDead;

    Train(EntityMinecart cart) {
        this(UUID.randomUUID(),
                Collections.singleton(cart.getPersistentID()),
                Collections.emptySet());
        this.world = cart.world;
        rebuild(cart);
    }

    Train(UUID id, Collection<UUID> carts, Set<UUID> locks) {
        this.uuid = id;
        this.carts.addAll(carts);
        this.locks.addAll(locks);
    }

    public static void printDebug(String msg, Object... args) {
        Game.log().msg(Level.DEBUG, msg, args);
    }

    private static Optional<Manager> getManager(@Nullable World world) {
        return Manager.forWorld(world);
    }

    public static Optional<Train> get(@Nullable EntityMinecart cart) {
        if (cart == null)
            return Optional.empty();
        return getManager(cart.world).map(manager -> {
            Game.requiresServerThread();
            Train train = manager.get(getTrainUUID(cart));
            if (train == null) {
                train = new Train(cart);
                manager.put(train.uuid, train);
                printDebug("Creating new train object: {0}", train);
            } else {
                train.world = cart.world;
                if (train.isDead || !train.contains(cart) || train.isInvalid()) {
                    train.kill();
                    return null;
                }
            }
            return train;
        });
    }

    private static Optional<Train> getTrainRaw(@Nullable EntityMinecart cart) {
        if (cart == null)
            return Optional.empty();
        Optional<Train> train = getManager(cart.world).map(manager -> manager.get(getTrainUUID(cart)));
        train.ifPresent(t -> t.world = cart.world);
        return train;
    }

    @Override
    public String toString() {
        return String.format("Train{id=%s,n=%d}", uuid, size());
    }

    /**
     * Will stream all carts in the train if on the server, or just the passed in cart if on the client.
     */
    public static Stream<EntityMinecart> streamCarts(EntityMinecart cart) {
        return get(cart).map(Train::stream).orElseGet(() -> Stream.of(cart));
    }

    public static @Nullable
    UUID getTrainUUID(EntityMinecart cart) {
        NBTTagCompound nbt = cart.getEntityData();
        return NBTPlugin.readUUID(nbt, TRAIN_NBT);
    }

    public static boolean isPartOfTrain(EntityMinecart cart) {
        return Train.get(cart).map(t -> t.size() > 1).orElse(false);
    }

    public static boolean areInSameTrain(@Nullable EntityMinecart cart1, @Nullable EntityMinecart cart2) {
        if (cart1 == null || cart2 == null)
            return false;
        if (cart1 == cart2)
            return true;

        UUID train1 = getTrainUUID(cart1);
        UUID train2 = getTrainUUID(cart2);

        return train1 != null && Objects.equals(train1, train2);
    }

    private static Optional<Train> getLongerTrain(EntityMinecart cart1, EntityMinecart cart2) {
        Optional<Train> train1 = getTrainRaw(cart1);
        Optional<Train> train2 = getTrainRaw(cart2);

        if (train1.equals(train2))
            return train1;
        if (!train1.isPresent())
            return train2;
        if (!train2.isPresent())
            return train1;

        if (train1.get().size() >= train2.get().size())
            return train1;
        return train2;
    }

    public static void repairTrain(EntityMinecart cart1, EntityMinecart cart2) {
        getLongerTrain(cart1, cart2).ifPresent(t -> t.rebuild(cart1));
    }

    public static void removeTrainTag(EntityMinecart cart) {
        cart.getEntityData().removeTag(TRAIN_NBT);
    }

    public void addTrainTag(EntityMinecart cart) {
        UUID trainId = getUUID();
        NBTPlugin.writeUUID(cart.getEntityData(), TRAIN_NBT, trainId);
    }

    private @Nullable
    EntityMinecart getCart(UUID cartID) {
        Objects.requireNonNull(world);
        return CartTools.getCartFromUUID(world, cartID);
    }

    private void rebuild(EntityMinecart first) {
        clear();
        rebuild(null, first);
        markDirty();
    }

    private void rebuild(@Nullable EntityMinecart prev, EntityMinecart next) {
        if (prev == null || carts.getFirst() == prev.getPersistentID())
            carts.addFirst(next.getPersistentID());
        else if (carts.getLast() == prev.getPersistentID())
            carts.addLast(next.getPersistentID());
        else
            throw new IllegalStateException("Passed a non-null prev value on an empty train!");

        getTrainRaw(next).filter(t -> t != this).ifPresent(Train::kill);
        addTrainTag(next);

        CouplingsDao lm = CouplingsDao.SERVER_INSTANCE;
        EntityMinecart coupledCartA = lm.getCoupledCartA(next);
        EntityMinecart coupledCartB = lm.getCoupledCartB(next);

        if (coupledCartA != null && coupledCartA != prev && !contains(coupledCartA))
            rebuild(next, coupledCartA);

        if (coupledCartB != null && coupledCartB != prev && !contains(coupledCartB))
            rebuild(next, coupledCartB);
    }

    private boolean isInvalid() {
        return isEmpty() || carts.stream().anyMatch(this::isCartInvalid);
    }

    private boolean isCartInvalid(UUID cartID) {
        EntityMinecart cart = getCart(cartID);
        return cart != null && !uuid.equals(getTrainUUID(cart));
    }

    public void kill() {
        isDead = true;
    }

    private void clear() {
        forEach(Train::removeTrainTag);
        carts.clear();
        markDirty();
    }

    public UUID getUUID() {
        return uuid;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean contains(@Nullable EntityMinecart cart) {
        return cart != null && carts.contains(cart.getPersistentID());
    }

    public boolean contains(@Nullable UUID cart) {
        return cart != null && carts.contains(cart);
    }

    public boolean isTrainEnd(@Nullable EntityMinecart cart) {
        return cart != null && getEnds().contains(cart.getPersistentID());
    }

    public Collection<UUID> getEnds() {
        Set<UUID> ends = new HashSet<>();
        if (!carts.isEmpty()) {
            ends.add(carts.getFirst());
            ends.add(carts.getLast());
        }
        return ends;
    }

    public Stream<EntityMinecart> stream() {
        return safeCarts.stream()
                .map(this::getCart)
                .filter(Objects::nonNull);
    }

    public <T extends EntityMinecart> Stream<T> stream(Class<T> cartClass) {
        return stream().flatMap(Streams.toType(cartClass));
    }

    @Override
    public Iterator<EntityMinecart> iterator() {
        return stream().iterator();
    }

    public <T extends EntityMinecart> List<T> getCarts(Class<T> cartClass) {
        return stream(cartClass).collect(Collectors.toList());
    }

    public List<UUID> getUUIDs() {
        return safeCarts;
    }

    public int size() {
        return carts.size();
    }

    public boolean isEmpty() {
        return carts.isEmpty();
    }

    public void refreshMaxSpeed() {
        setMaxSpeed(calculateMaxSpeed());
    }

    private float calculateMaxSpeed() {
        return (float) stream().mapToDouble(EntityMinecart::getMaxCartSpeedOnRail).min().orElse(1.2F);
    }

    private void setMaxSpeed(float trainSpeed) {
        for (EntityMinecart c : this) {
            c.setCurrentCartSpeedCapOnRail(trainSpeed);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Train)) {
            return false;
        }
        Train other = (Train) obj;
        return uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    static @Nullable
    Train readFromNBT(NBTTagCompound tag) {
        UUID id = NBTPlugin.readUUID(tag, "id");
        if (id == null)
            return null;
        List<UUID> carts = NBTPlugin.getNBTList(tag, "carts", NBTTagCompound.class).stream().map(NBTUtil::getUUIDFromTag).collect(Collectors.toList());
        Set<UUID> locks = NBTPlugin.getNBTList(tag, "locks", NBTTagCompound.class).stream().map(NBTUtil::getUUIDFromTag).collect(Collectors.toSet());
        return new Train(id, carts, locks);
    }

    void writeToNBT(NBTTagCompound tag) {
        NBTPlugin.writeUUID(tag, "id", uuid);
        NBTTagList listTag = new NBTTagList();
        for (UUID uuid : carts) {
            listTag.appendTag(NBTUtil.createUUIDTag(uuid));
        }
        tag.setTag("carts", listTag);

        NBTTagList locks = new NBTTagList();
        for (UUID uuid : this.locks) {
            locks.appendTag(NBTUtil.createUUIDTag(uuid));
        }
        tag.setTag("locks", locks);
    }

    void markDirty() {
        setDirty(true);
    }

    boolean isDirty() {
        return dirty;
    }

    void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public static final class Manager extends ForwardingMap<UUID, Train> {

        private static final Map<World, Manager> instances = new MapMaker().weakKeys().makeMap();

        final World world;
        final SaveData data;

        private Manager(World world) {
            this.world = world;
            this.data = makeData(world);
        }

        private static Optional<Manager> forWorld(@Nullable World world) {
            if (world == null || Game.isClient(world))
                return Optional.empty();
            return Optional.of(instances.computeIfAbsent(world, Manager::new));
        }

        private static SaveData makeData(World world) {
            MapStorage storage = world.getPerWorldStorage();
            SaveData data = (SaveData) storage.getOrLoadData(SaveData.class, "railcraft.trains");
            if (data == null) {
                data = new SaveData("railcraft.trains");
                storage.setData("railcraft.trains", data);
            }
            return data;
        }

        public static void clearTrains() {
            instances.values().forEach(ForwardingMap::clear);
        }

        @Override
        protected Map<UUID, Train> delegate() {
            return data.trains;
        }

    }

    public static final class SaveData extends WorldSavedData {
        final Map<UUID, Train> trains = new ForwardingMap<UUID, Train>() {
            private final Map<UUID, Train> trains = new HashMap<>();

            @Override
            protected Map<UUID, Train> delegate() {
                return trains;
            }

            @Override
            public Train put(@NotNull UUID key, @NotNull Train value) {
                markDirty();
                return super.put(key, value);
            }

            @Override
            public void putAll(@NotNull Map<? extends UUID, ? extends Train> map) {
                standardPutAll(map);
            }

            @Override
            public Train remove(@NotNull Object key) {
                markDirty();
                return super.remove(key);
            }

            @Override
            public void clear() {
                super.clear();
                markDirty();
            }
        };

        public SaveData(String name) {
            super(name);
        }

        @Override
        public void readFromNBT(@NotNull NBTTagCompound nbt) {
            trains.clear();
            for (NBTTagCompound each : NBTPlugin.getNBTList(nbt, "trains", NBTTagCompound.class)) {
                Train train = Train.readFromNBT(each);
                if (train != null)
                    trains.put(train.getUUID(), train);
            }
            printDebug("Loaded {0} Trains...", trains.size());
        }

        @Override
        public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
            printDebug("Saving {0} Trains...", trains.size());
            NBTTagList listTag = new NBTTagList();
            for (Train train : trains.values()) {
                NBTTagCompound tag = new NBTTagCompound();
                train.writeToNBT(tag);
                listTag.appendTag(tag);
                train.setDirty(false);
            }
            compound.setTag("trains", listTag);
            return compound;
        }

        @Override
        public boolean isDirty() {
            return super.isDirty() || trains.values().stream().anyMatch(Train::isDirty);
        }
    }
}
