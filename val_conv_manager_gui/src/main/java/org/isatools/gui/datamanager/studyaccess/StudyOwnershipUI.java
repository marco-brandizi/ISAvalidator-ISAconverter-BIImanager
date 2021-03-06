/**

 The ISAconverter, ISAvalidator & BII Management Tool are components of the ISA software suite (http://www.isa-tools.org)

 Exhibit A
 The ISAconverter, ISAvalidator & BII Management Tool are licensed under the Mozilla Public License (MPL) version
 1.1/GPL version 2.0/LGPL version 2.1

 "The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"). You may not use this file except in compliance with the License.
 You may obtain copies of the Licenses at http://www.mozilla.org/MPL/MPL-1.1.html.

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is the ISAconverter, ISAvalidator & BII Management Tool.

 The Initial Developer of the Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
 Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
 http://www.isa-tools.org). All portions of the code written by the ISA Team are Copyright (c)
 2007-2011 ISA Team. All Rights Reserved.

 Contributor(s):
 Rocca-Serra P, Brandizi M, Maguire E, Sklyar N, Taylor C, Begley K, Field D,
 Harris S, Hide W, Hofmann O, Neumann S, Sterk P, Tong W, Sansone SA. ISA software suite:
 supporting standards-compliant experimental annotation and enabling curation at the community level.
 Bioinformatics 2010;26(18):2354-6.

 Alternatively, the contents of this file may be used under the terms of either the GNU General
 Public License Version 2 or later (the "GPL") - http://www.gnu.org/licenses/gpl-2.0.html, or
 the GNU Lesser General Public License Version 2.1 or later (the "LGPL") -
 http://www.gnu.org/licenses/lgpl-2.1.html, in which case the provisions of the GPL
 or the LGPL are applicable instead of those above. If you wish to allow use of your version
 of this file only under the terms of either the GPL or the LGPL, and not to allow others to
 use your version of this file under the terms of the MPL, indicate your decision by deleting
 the provisions above and replace them with the notice and other provisions required by the
 GPL or the LGPL. If you do not delete the provisions above, a recipient may use your version
 of this file under the terms of any one of the MPL, the GPL or the LGPL.

 Sponsors:
 The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics project
 (http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC
 (http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).

 */

package org.isatools.gui.datamanager.studyaccess;

import org.isatools.effects.ExtendedJList;
import org.isatools.effects.UIHelper;
import org.isatools.gui.AppContainer;
import org.isatools.gui.Globals;
import org.isatools.gui.ViewingPane;
import org.isatools.gui.datamanager.ColumnFilterRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * StudyOwnershipUI
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Nov 16, 2009
 */


public class StudyOwnershipUI extends ViewingPane {

    private ExtendedJList studyList;
    private JList availableUsersList;
    private JList selectedUsers;

    private String[] studies;
    private String[] users;

    // mapping between study and selected usernames.
    private Map<String, Set<String>> studyToUser;

    private ControlPanel controlPanel;


    public StudyOwnershipUI(AppContainer container, String[] studies, String[] users) {
        super(container);
        this.studies = studies;
        this.users = users;

        setPreferredSize(new Dimension(400, 250));
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setOpaque(false);
    }

    private void instantiateStudyToUserMap() {
        studyToUser = new HashMap<String, Set<String>>();

        for (String study : studies) {
            studyToUser.put(study, new HashSet<String>());
        }
    }


    public void createGUI() {
        instantiateStudyToUserMap();

        // need to create 3 JLists with elements for available studies, available users and selected users.
        studyList = new ExtendedJList();
        createListContents(studyList, studies);
        studyList.setOpaque(false);

        studyList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                // we want to refresh the available user list and the selected user list.
                if (!studyList.isSelectionEmpty()) {
                    updateSelectedStudyItems();
                } else {
                    createListContents(availableUsersList, users);
                    createListContents(selectedUsers, new String[]{});
                }
                updateButtonActivity();
            }
        });

        availableUsersList = new JList();
        availableUsersList.setOpaque(false);
        createListContents(availableUsersList, users);

        selectedUsers = new JList();
        selectedUsers.setOpaque(false);

        add(createListContainer(studyList, "studies", new ColumnFilterRenderer(UIHelper.VER_11_BOLD,
                UIHelper.LIGHT_GREEN_COLOR, UIHelper.VER_11_PLAIN, UIHelper.LIGHT_GREY_COLOR, Globals.LIST_ICON_STUDY)));
        // add available user list to the UI
        add(createListContainer(availableUsersList, "available users", new ColumnFilterRenderer(UIHelper.VER_11_BOLD,
                UIHelper.LIGHT_GREEN_COLOR, UIHelper.VER_11_PLAIN, UIHelper.LIGHT_GREY_COLOR,
                Globals.LIST_ICON_USER)));

        add(createControlPanel());

        // add selected user list to the UI
        add(createListContainer(selectedUsers, "selected users", new ColumnFilterRenderer(UIHelper.VER_11_BOLD,
                UIHelper.LIGHT_GREEN_COLOR, UIHelper.VER_11_PLAIN, UIHelper.LIGHT_GREY_COLOR,
                Globals.LIST_ICON_USER)));

        selectFirstItemInList(studyList);
        selectFirstItemInList(availableUsersList);
    }

    private void updateSelectedStudyItems() {

        if (!studyList.isSelectionEmpty()) {
            String selectedItem = studyList.getSelectedValue().toString();
            if (studyToUser.containsKey(selectedItem)) {
                Set<String> selectedUsersSet = studyToUser.get(selectedItem);
                createListContents(availableUsersList,
                        getListDifference(users, selectedUsersSet.toArray(new String[selectedUsersSet.size()])));

                createListContents(selectedUsers, selectedUsersSet.toArray(
                        new String[selectedUsersSet.size()]));
            } else {
                createListContents(availableUsersList, users);
                createListContents(selectedUsers, new String[]{});
            }
        }

        updateButtonActivity();

    }

    private void createListContents(JList list, Object[] contents) {

        if (list instanceof ExtendedJList) {
            ExtendedJList xList = (ExtendedJList) list;
            xList.clearItems();

            for (Object value : contents) {
                xList.addItem(value);
            }

        } else {
            DefaultListModel model = new DefaultListModel();
            for (Object value : contents) {
                model.addElement(value);
            }

            list.setModel(model);
        }

        list.revalidate();
    }

    private void selectFirstItemInList(JList list) {
        if (list.getModel().getSize() > 0) {
            list.setSelectedIndex(0);
        }
    }

    private void addUserToStudy(String study, String user) {
        if (!studyToUser.containsKey(study)) {
            studyToUser.put(study, new HashSet<String>());
        }

        studyToUser.get(study).add(user);

        String[] studyUsersAsArray = getAvailableUsers(study);

        createListContents(selectedUsers, studyUsersAsArray);

        updateButtonActivity();

        selectFirstItemInList(availableUsersList);
    }

    private void removeUserFromStudy(String study, String user) {
        if (studyToUser.containsKey(study)) {
            if (studyToUser.get(study).contains(user)) {
                studyToUser.get(study).remove(user);
            }
        }

        String[] studyUsersAsArray = getAvailableUsers(study);

        createListContents(selectedUsers, studyUsersAsArray);

        updateButtonActivity();

        selectFirstItemInList(selectedUsers);
    }

    private void addAllUsersToStudy(String study) {
        if (!studyToUser.containsKey(study)) {
            studyToUser.put(study, new HashSet<String>());
        }

        studyToUser.get(study).addAll(Arrays.asList(users));

        createListContents(selectedUsers, users);
        createListContents(availableUsersList, new String[]{});

        updateButtonActivity();

        selectFirstItemInList(selectedUsers);
    }

    private void removeAllUsersFromStudy(String study) {
        if (studyToUser.containsKey(study)) {
            studyToUser.get(study).clear();
        }

        createListContents(selectedUsers, new String[]{});
        createListContents(availableUsersList, users);

        updateButtonActivity();

        selectFirstItemInList(availableUsersList);
    }

    /**
     * Return the users which can be added to the selected users list for the study
     *
     * @param study - the study being configured
     * @return String[] containing the users which can be added
     */
    private String[] getAvailableUsers(String study) {

        Set<String> studyUsers = studyToUser.get(study);
        String[] studyUsersAsArray = studyUsers.toArray(new String[studyUsers.size()]);
        createListContents(availableUsersList,
                getListDifference(users, studyUsersAsArray));

        return studyUsers.toArray(new String[studyUsers.size()]);
    }

    /**
     * Update whether or not one may add or remove
     */
    private void updateButtonActivity() {
        boolean canAdd = availableUsersList.getModel().getSize() != 0;
        controlPanel.setButtonEnabled(ControlPanel.ADD, canAdd);
        controlPanel.setButtonEnabled(ControlPanel.ADD_ALL, canAdd);

        boolean canRemove = selectedUsers.getModel().getSize() != 0;
        controlPanel.setButtonEnabled(ControlPanel.REMOVE, canRemove);
        controlPanel.setButtonEnabled(ControlPanel.REMOVE_ALL, canRemove);

        this.repaint();
    }

    private JPanel createControlPanel() {
        controlPanel = new ControlPanel();
        controlPanel.createGUI();

        controlPanel.addPropertyChangeListener(ControlPanel.ADD, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (!studyList.isSelectionEmpty()) {
                    if (!availableUsersList.isSelectionEmpty()) {
                        addUserToStudy(studyList.getSelectedValue().toString(), availableUsersList.getSelectedValue().toString());
                    }
                }
            }
        });

        controlPanel.addPropertyChangeListener(ControlPanel.REMOVE, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (!studyList.isSelectionEmpty()) {
                    if (!selectedUsers.isSelectionEmpty()) {
                        removeUserFromStudy(studyList.getSelectedValue().toString(), selectedUsers.getSelectedValue().toString());
                    }
                }
            }
        });

        controlPanel.addPropertyChangeListener(ControlPanel.ADD_ALL, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (!studyList.isSelectionEmpty()) {
                    addAllUsersToStudy(studyList.getSelectedValue().toString());
                }

            }
        });

        controlPanel.addPropertyChangeListener(ControlPanel.REMOVE_ALL, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (!studyList.isSelectionEmpty()) {
                    removeAllUsersFromStudy(studyList.getSelectedValue().toString());
                }
            }
        });

        controlPanel.setButtonEnabled(ControlPanel.REMOVE, false);
        controlPanel.setButtonEnabled(ControlPanel.REMOVE_ALL, false);

        return controlPanel;
    }

    private String[] getListDifference(String[] originalItems, String[] selectedItems) {

        Set<String> items = new HashSet<String>();
        items.addAll(Arrays.asList(originalItems));

        for (String selected : selectedItems) {
            if (items.contains(selected)) {
                items.remove(selected);
            }
        }

        return items.toArray(new String[items.size()]);
    }


    public Map<String, Set<String>> getStudyToUser() {
        return studyToUser;
    }

    public void setStudyToUser(Map<String, Set<String>> studyToUser) {
        this.studyToUser = studyToUser;
        updateSelectedStudyItems();
    }

}
