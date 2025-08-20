package be.twofold.tinybcdec;

/**
 * Specifies the data format for BCn texture compression channels.
 * Determines whether compressed values are interpreted as signed or unsigned.
 */
public enum Signedness {
    /**
     * Values are interpreted as signed data.
     */
    SIGNED,

    /**
     * Values are interpreted as unsigned data.
     */
    UNSIGNED,
}
