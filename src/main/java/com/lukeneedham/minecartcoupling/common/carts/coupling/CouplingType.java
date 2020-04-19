package com.lukeneedham.minecartcoupling.common.carts.coupling;

public enum CouplingType {
    COUPLING_A(Consts.COUPLING_A_TAG_HIGH, Consts.COUPLING_A_TAG_LOW),
    COUPLING_B(Consts.COUPLING_B_TAG_HIGH, Consts.COUPLING_B_TAG_LOW);

    public static final CouplingType[] VALUES = values();

    public final String tagHigh;
    public final String tagLow;

    CouplingType(String tagHigh, String tagLow) {
        this.tagHigh = tagHigh;
        this.tagLow = tagLow;
    }

    private static class Consts {
        private static final String COUPLING_A_TAG_HIGH = "rcCouplingAHigh";
        private static final String COUPLING_A_TAG_LOW = "rcCouplingALow";
        private static final String COUPLING_B_TAG_HIGH = "rcCouplingBHigh";
        private static final String COUPLING_B_TAG_LOW = "rcCouplingBLow";
    }
}