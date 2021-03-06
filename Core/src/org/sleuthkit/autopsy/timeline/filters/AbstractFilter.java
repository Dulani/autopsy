/*
 * Autopsy Forensic Browser
 *
 * Copyright 2014 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.timeline.filters;

import javafx.beans.property.SimpleBooleanProperty;

/**
 * Base implementation of a {@link Filter}. Implements active property.
 *
 */
public abstract class AbstractFilter implements Filter {

    private final SimpleBooleanProperty selected = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty disabled = new SimpleBooleanProperty(false);

    @Override
    public SimpleBooleanProperty getSelectedProperty() {
        return selected;
    }

    @Override
    public SimpleBooleanProperty getDisabledProperty() {
        return disabled;
    }

    @Override
    public void setSelected(Boolean act) {
        selected.set(act);
    }

    @Override
    public boolean isSelected() {
        return selected.get();
    }

    @Override
    public void setDisabled(Boolean act) {
        disabled.set(act);
    }

    @Override
    public boolean isDisabled() {
        return disabled.get();
    }

    @Override
    public String getStringCheckBox() {
        return "[" + (isSelected() ? "x" : " ") + "]"; // NON-NLS
    }

}
