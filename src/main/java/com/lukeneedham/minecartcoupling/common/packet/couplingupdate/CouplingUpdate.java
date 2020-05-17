package com.lukeneedham.minecartcoupling.common.packet.couplingupdate;

public abstract class CouplingUpdate {
    public int type;

    public static class Created extends CouplingUpdate {
        public static final int TYPE = 0;

        public int cart1Id;
        public int cart2Id;

        public Created(int cart1Id, int cart2Id) {
            this.type = TYPE;
            this.cart1Id = cart1Id;
            this.cart2Id = cart2Id;
        }
    }

    public static class Broken extends CouplingUpdate {
        public static final int TYPE = 1;

        public int cart1Id;
        public int cart2Id;

        public Broken(int cart1Id, int cart2Id) {
            this.type = TYPE;
            this.cart1Id = cart1Id;
            this.cart2Id = cart2Id;
        }
    }

    public static class AllBroken extends CouplingUpdate {
        public static final int TYPE = 2;

        public int cartId;

        public AllBroken(int cartId) {
            this.type = TYPE;
            this.cartId = cartId;
        }
    }
}