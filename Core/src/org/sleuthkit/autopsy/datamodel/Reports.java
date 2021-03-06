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
package org.sleuthkit.autopsy.datamodel;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.corecomponents.DataContentTopComponent;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.Report;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Implements the Reports subtree of the Autopsy tree.
 */
public final class Reports implements AutopsyVisitableItem {

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    @Override
    public <T> T accept(AutopsyItemVisitor<T> visitor) {
        // CreateAutopsyNodeVisitor.visit() constructs a ReportsListNode.
        return visitor.visit(this);
    }

    /**
     * The root node of the Reports subtree of the Autopsy tree.
     */
    public static final class ReportsListNode extends DisplayableItemNode {

        private static final String DISPLAY_NAME = NbBundle.getMessage(ReportsListNode.class, "ReportsListNode.displayName");
        private static final String ICON_PATH = "org/sleuthkit/autopsy/images/report_16.png"; //NON-NLS

        public ReportsListNode() {
            super(Children.create(new ReportNodeFactory(), true));
            setName(DISPLAY_NAME);
            setDisplayName(DISPLAY_NAME);
            this.setIconBaseWithExtension(ICON_PATH);
        }

        @Override
        public boolean isLeafTypeNode() {
            return true;
        }

        @Override
        public <T> T accept(DisplayableItemNodeVisitor<T> visitor) {
            // - GetPopupActionsDisplayableItemNodeVisitor.visit() returns null.
            // - GetPreferredActionsDisplayableItemNodeVisitor.visit() returns null.
            // - IsLeafItemVisitor.visit() returns false.
            // - ShowItemVisitor.visit() returns true.
            return visitor.visit(this);
        }
    }

    /**
     * The child node factory that creates ReportNode children for a
     * ReportsListNode.
     */
    private static final class ReportNodeFactory extends ChildFactory<Report> {

        ReportNodeFactory() {
            Case.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String eventType = evt.getPropertyName();
                    if (eventType.equals(Case.Events.REPORT_ADDED.toString()) || eventType.equals(Case.Events.REPORT_DELETED.toString())) {
                        ReportNodeFactory.this.refresh(true);
                    }
                }
            });
        }

        @Override
        protected boolean createKeys(List<Report> keys) {
            try {
                keys.addAll(Case.getCurrentCase().getAllReports());
            } catch (TskCoreException ex) {
                Logger.getLogger(Reports.ReportNodeFactory.class.getName()).log(Level.SEVERE, "Failed to get reports", ex); //NON-NLS
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(Report key) {
            return new ReportNode(key);
        }
    }

    /**
     * A leaf node in the Reports subtree of the Autopsy tree, wraps a Report
     * object.
     */
    public static final class ReportNode extends DisplayableItemNode {

        private static final String ICON_PATH = "org/sleuthkit/autopsy/images/report_16.png"; //NON-NLS
        private final Report report;

        ReportNode(Report report) {
            super(Children.LEAF, Lookups.fixed(report));
            this.report = report;
            super.setName(this.report.getSourceModuleName());
            super.setDisplayName(this.report.getSourceModuleName());
            this.setIconBaseWithExtension(ICON_PATH);
        }

        @Override
        public boolean isLeafTypeNode() {
            return true;
        }

        @Override
        public <T> T accept(DisplayableItemNodeVisitor<T> visitor) {
            // - GetPopupActionsDisplayableItemNodeVisitor.visit() calls getActions().
            // - GetPreferredActionsDisplayableItemNodeVisitor.visit() calls getPreferredAction().
            // - IsLeafItemVisitor.visit() returns true.
            // - ShowItemVisitor.visit() returns true.
            return visitor.visit(this);
        }

        @Override
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            Sheet.Set propertiesSet = sheet.get(Sheet.PROPERTIES);
            if (propertiesSet == null) {
                propertiesSet = Sheet.createPropertiesSet();
                sheet.put(propertiesSet);
            }
            propertiesSet.put(new NodeProperty<>(NbBundle.getMessage(this.getClass(), "ReportNode.sourceModuleNameProperty.name"),
                    NbBundle.getMessage(this.getClass(), "ReportNode.sourceModuleNameProperty.displayName"),
                    NbBundle.getMessage(this.getClass(), "ReportNode.sourceModuleNameProperty.desc"),
                    this.report.getSourceModuleName()));
            propertiesSet.put(new NodeProperty<>(NbBundle.getMessage(this.getClass(), "ReportNode.reportNameProperty.name"),
                    NbBundle.getMessage(this.getClass(), "ReportNode.reportNameProperty.displayName"),
                    NbBundle.getMessage(this.getClass(), "ReportNode.reportNameProperty.desc"),
                    this.report.getReportName()));
            propertiesSet.put(new NodeProperty<>(NbBundle.getMessage(this.getClass(), "ReportNode.createdTimeProperty.name"),
                    NbBundle.getMessage(this.getClass(), "ReportNode.createdTimeProperty.displayName"),
                    NbBundle.getMessage(this.getClass(), "ReportNode.createdTimeProperty.desc"),
                    dateFormatter.format(new java.util.Date(this.report.getCreatedTime() * 1000))));
            propertiesSet.put(new NodeProperty<>(NbBundle.getMessage(this.getClass(), "ReportNode.pathProperty.name"),
                    NbBundle.getMessage(this.getClass(), "ReportNode.pathProperty.displayName"),
                    NbBundle.getMessage(this.getClass(), "ReportNode.pathProperty.desc"),
                    this.report.getPath()));
            return sheet;
        }

        @Override
        public Action[] getActions(boolean popup) {
            List<Action> actions = new ArrayList<>();
            actions.addAll(Arrays.asList(super.getActions(true)));
            actions.add(new OpenReportAction());
            actions.add(DeleteReportAction.getInstance());
            return actions.toArray(new Action[actions.size()]);
        }

        @Override
        public AbstractAction getPreferredAction() {
            return new OpenReportAction();
        }

        private static class DeleteReportAction extends AbstractAction {

            private static DeleteReportAction instance;

            // This class is a singleton to support multi-selection of nodes,
            // since org.openide.nodes.NodeOp.findActions(Node[] nodes) will
            // only pick up an Action if every node in the array returns a
            // reference to the same action object from Node.getActions(boolean).
            private static DeleteReportAction getInstance() {
                if (instance == null) {
                    instance = new DeleteReportAction();
                }
                if (Utilities.actionsGlobalContext().lookupAll(Report.class).size() == 1) {
                    instance.putValue(Action.NAME, NbBundle.getMessage(Reports.class, "DeleteReportAction.actionDisplayName.singleReport"));
                } else {
                    instance.putValue(Action.NAME, NbBundle.getMessage(Reports.class, "DeleteReportAction.actionDisplayName.multipleReports"));
                }
                return instance;
            }

            /**
             * Do not instantiate directly. Use
             * DeleteReportAction.getInstance(), instead.
             */
            private DeleteReportAction() {
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Collection<? extends Report> selectedReportsCollection = Utilities.actionsGlobalContext().lookupAll(Report.class);

                String jOptionPaneMessage = selectedReportsCollection.size() > 1
                        ? NbBundle.getMessage(Reports.class, "DeleteReportAction.actionPerformed.showConfirmDialog.multiple.msg", selectedReportsCollection.size())
                        : NbBundle.getMessage(Reports.class, "DeleteReportAction.actionPerformed.showConfirmDialog.single.msg");
                JCheckBox checkbox = new JCheckBox(NbBundle.getMessage(Reports.class, "DeleteReportAction.actionPerformed.showConfirmDialog.checkbox.msg"));
                checkbox.setSelected(false);

                Object[] jOptionPaneContent = {jOptionPaneMessage, checkbox};

                if (JOptionPane.showConfirmDialog(null, jOptionPaneContent,
                        NbBundle.getMessage(Reports.class, "DeleteReportAction.actionPerformed.showConfirmDialog.title"),
                        JOptionPane.YES_NO_OPTION) == 0) {
                    try {
                        Case.getCurrentCase().deleteReports(selectedReportsCollection, checkbox.isSelected());
                        DataContentTopComponent.findInstance().repaint();
                    } catch (TskCoreException | IllegalStateException ex) {
                        Logger.getLogger(DeleteReportAction.class.getName()).log(Level.INFO, "Error deleting the reports. ", ex); // NON-NLS - Provide solution to the user?
                    }
                }
            }
        }

        private final class OpenReportAction extends AbstractAction {

            private OpenReportAction() {
                super(NbBundle.getMessage(OpenReportAction.class, "OpenReportAction.actionDisplayName"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                File file = new File(ReportNode.this.report.getPath());
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null,
                            NbBundle.getMessage(OpenReportAction.class, "OpenReportAction.actionPerformed.NoAssociatedEditorMessage"),
                            NbBundle.getMessage(OpenReportAction.class, "OpenReportAction.actionPerformed.MessageBoxTitle"),
                            JOptionPane.ERROR_MESSAGE);
                } catch (UnsupportedOperationException ex) {
                    JOptionPane.showMessageDialog(null,
                            NbBundle.getMessage(OpenReportAction.class, "OpenReportAction.actionPerformed.NoOpenInEditorSupportMessage"),
                            NbBundle.getMessage(OpenReportAction.class, "OpenReportAction.actionPerformed.MessageBoxTitle"),
                            JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(null,
                            NbBundle.getMessage(OpenReportAction.class, "OpenReportAction.actionPerformed.MissingReportFileMessage"),
                            NbBundle.getMessage(OpenReportAction.class, "OpenReportAction.actionPerformed.MessageBoxTitle"),
                            JOptionPane.ERROR_MESSAGE);
                } catch (SecurityException ex) {
                    JOptionPane.showMessageDialog(null,
                            NbBundle.getMessage(OpenReportAction.class, "OpenReportAction.actionPerformed.ReportFileOpenPermissionDeniedMessage"),
                            NbBundle.getMessage(OpenReportAction.class, "OpenReportAction.actionPerformed.MessageBoxTitle"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
