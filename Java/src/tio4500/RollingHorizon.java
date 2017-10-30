package tio4500;

import com.dashoptimization.*;

public class RollingHorizon {

    public RollingHorizon() {};

    public void run() {
        XPRM mosel;
        XPRMModel mod;
        XPRMSet set;

        mosel = new XPRM();
        try {
            mosel.compile("../Mosel/test.mos");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
