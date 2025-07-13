package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.PDCRoseSetting;
import java.util.List;

public interface PDCSettingHolder {

    List<PDCRoseSetting<?>> get();

}
