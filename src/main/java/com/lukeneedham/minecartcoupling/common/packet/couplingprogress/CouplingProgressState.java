package com.lukeneedham.minecartcoupling.common.packet.couplingprogress;

public abstract class CouplingProgressState {
    public int type;
    public int cartId;
    public int playerId;

    public static class Started extends CouplingProgressState {
        public static final int TYPE = 0;

        public Started(int cartId, int playerId) {
            this.type = TYPE;
            this.cartId = cartId;
            this.playerId = playerId;
        }
    }

    public static class Broken extends CouplingProgressState {
        public static final int TYPE = 1;

        public Broken(int cartId, int playerId) {
            this.type = TYPE;
            this.cartId = cartId;
            this.playerId = playerId;
        }
    }

    public static class Created extends CouplingProgressState {
        public static final int TYPE = 2;

        public Created(int cartId, int playerId) {
            this.type = TYPE;
            this.cartId = cartId;
            this.playerId = playerId;
        }
    }

    public static class Failed extends CouplingProgressState {
        public static final int TYPE = 3;

        public Failed(int cartId, int playerId) {
            this.type = TYPE;
            this.cartId = cartId;
            this.playerId = playerId;
        }
    }

    public static CouplingProgressState from(int type, int cartId, int playerId) {
        CouplingProgressState state;
        switch (type) {
            case Started.TYPE: {
                state = new Started(cartId, playerId);
                break;
            }
            case Broken.TYPE: {
                state = new Broken(cartId, playerId);
                break;
            }
            case Created.TYPE: {
                state = new Created(cartId, playerId);
                break;
            }
            case Failed.TYPE: {
                state = new Failed(cartId, playerId);
                break;
            }
            default: {
                state = null;
                break;
            }
        }
        if (state == null) {
            throw new RuntimeException("Unhandled MinecartCouplingState type: " + type);
        }
        return state;
    }
}