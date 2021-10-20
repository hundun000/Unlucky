package com.unlucky.battle;
/**
 * @author hundun
 * Created on 2021/10/21
 */
public enum MoveType {
    ACCURATE(0),
    WIDE(1),
    CRIT(2),
    HEALING(3),
    ;
    
    final int code;
    
    private MoveType(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }

    public static MoveType fromCode(int code) {
        for (MoveType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new RuntimeException();
    }
    
    public boolean isAccurateOrWide() {
        return this == MoveType.ACCURATE || this == MoveType.WIDE;
    }
    
    public boolean isAttack() {
        return this != HEALING;
    }
}
