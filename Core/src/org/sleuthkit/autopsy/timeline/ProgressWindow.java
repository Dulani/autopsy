/*
 * Autopsy Forensic Browser
 *
 * Copyright 2013 Basis Technology Corp.
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
package org.sleuthkit.autopsy.timeline;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.annotation.concurrent.Immutable;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Dialog with progress bar that pops up when timeline is being generated
 */
public class ProgressWindow extends JFrame {

    private final SwingWorker<?, ?> worker;

    /**
     * Creates new form TimelineProgressDialog
     */
    public ProgressWindow(Component parent, boolean modal, SwingWorker<?, ?> worker) {
        super();
        initComponents();

        setLocationRelativeTo(parent);

        setAlwaysOnTop(modal);

        //set icon the same as main app
        SwingUtilities.invokeLater(() -> {
            setIconImage(WindowManager.getDefault().getMainWindow().getIconImage());
        });

        //progressBar.setIndeterminate(true);
        setName(NbBundle.getMessage(TimeLineTopComponent.class, "Timeline.progressWindow.name"));
        setTitle(NbBundle.getMessage(TimeLineTopComponent.class, "Timeline.progressWindow.title"));
        // Close the dialog when Esc is pressed
        String cancelName = "cancel"; // NON-NLS
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = getRootPane().getActionMap();

        actionMap.put(cancelName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        this.worker = worker;
    }

    public void updateProgress(final int progress) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
        });
    }

    public void updateProgress(final int progress, final String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
            progressBar.setString(message);
        });
    }

    public void updateProgress(final String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setString(message);
        });
    }

    public void setProgressTotal(final int total) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(total);
            progressBar.setStringPainted(true);
        });
    }

    public void updateHeaderMessage(final String headerMessage) {
        SwingUtilities.invokeLater(() -> {
            progressHeader.setText(headerMessage);
        });
    }

    public void setIndeterminate() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(true);
            progressBar.setStringPainted(true);
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        progressBar = new JProgressBar();
        progressHeader = new JLabel();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeDialog(evt);
            }
        });

        Mnemonics.setLocalizedText(progressHeader, NbBundle.getMessage(ProgressWindow.class, "ProgressWindow.progressHeader.text")); // NOI18N

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(progressHeader)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(progressHeader)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        cancel();
    }//GEN-LAST:event_closeDialog

    public void cancel() {
        SwingUtilities.invokeLater(() -> {
            if (isVisible()) {
                int showConfirmDialog = JOptionPane.showConfirmDialog(ProgressWindow.this,
                        NbBundle.getMessage(TimeLineTopComponent.class,
                                "Timeline.ProgressWindow.cancel.confdlg.msg"),
                        NbBundle.getMessage(TimeLineTopComponent.class,
                                "Timeline.ProgressWindow.cancel.confdlg.detail"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (showConfirmDialog == JOptionPane.YES_OPTION) {
                    close();
                }
            } else {
                close();
            }
        });
    }

    public void close() {
        worker.cancel(false);
        setVisible(false);
        dispose();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JProgressBar progressBar;
    private JLabel progressHeader;
    // End of variables declaration//GEN-END:variables

    public void update(ProgressUpdate chunk) {
        updateHeaderMessage(chunk.getHeaderMessage());
        if (chunk.getTotal() >= 0) {
            setProgressTotal(chunk.getTotal());
            updateProgress(chunk.getProgress(), chunk.getDetailMessage());
        } else {
            setIndeterminate();
            updateProgress(chunk.getDetailMessage());
        }
    }

    /**
     * bundles up progress information to be shown in the progress dialog
     */
    @Immutable
    public static class ProgressUpdate {

        private final int progress;
        private final int total;
        private final String headerMessage;
        private final String detailMessage;

        public int getProgress() {
            return progress;
        }

        public int getTotal() {
            return total;
        }

        public String getHeaderMessage() {
            return headerMessage;
        }

        public String getDetailMessage() {
            return detailMessage;
        }

        public ProgressUpdate(int progress, int total, String headerMessage, String detailMessage) {
            super();
            this.progress = progress;
            this.total = total;
            this.headerMessage = headerMessage;
            this.detailMessage = detailMessage;
        }
    }
}
