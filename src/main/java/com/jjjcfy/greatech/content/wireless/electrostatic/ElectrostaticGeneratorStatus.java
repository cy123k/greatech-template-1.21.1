package com.jjjcfy.greatech.content.wireless.electrostatic;

public enum ElectrostaticGeneratorStatus {
    STOPPED("stopped"),
    CHARGING_POOL("charging_pool"),
    CHARGING_POOL_LOW_RPM("charging_pool_low_rpm"),
    DISCHARGING_POOL("discharging_pool"),
    NO_COILS("no_coils"),
    SPEED_TOO_LOW("speed_too_low"),
    POOL_FULL("pool_full"),
    POOL_EMPTY("pool_empty"),
    ENERGY_SIDE_UNAVAILABLE("energy_side_unavailable"),
    BUFFER_EMPTY("buffer_empty"),
    BUFFER_FULL("buffer_full");

    private final String id;

    ElectrostaticGeneratorStatus(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
