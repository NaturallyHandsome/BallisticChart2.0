package sample;

/*
    To = sea level standard Temp in Kelvins
    Lk = temperature lapse rate in K/km
    Re = Earth radius in m
    Rg = universal gas constant
    Ma = molecular weight of dry air in gram per mole
    Po = standard sea level pressure in Pa
    g = gravity force
    Co =  speed of sound at 1K
    AirD = air density at 20°C sea level
    metricConversion = metric conversion for section area
    calToMetric = caliber to metric conversion factor
    grToKg = grains to Kg conversion factor
    airLiftCorrection = average longitudinal section area/ diameter section area
 */

public enum PhyConstants {
    TO(288.15), LK(6.5), RE(6356766D), RG(8.31432), MA(28.9644), PO(101325.0), G(9.80665), C0(20.046), AIRDENSITY(1.2041),
    METRICONVERSION(0.00142231040564373897707231040564), CALTOMETRIC(0.0254), GRTOKG(0.00006479891), AIRLIFTCORRECTION(1.63),
    MACH_MS(340.294065), MACH_FPS(1116.450345), MS_FPS(3.28084);

    private Double constantAsDouble;

    PhyConstants(Double constantAsDouble) {
        this.constantAsDouble = constantAsDouble;
    }

    public Double get() {
        return this.constantAsDouble;
    }
}
