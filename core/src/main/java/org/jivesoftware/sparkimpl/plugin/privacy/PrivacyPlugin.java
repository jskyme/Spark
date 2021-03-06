/**
 * Copyright (C) 2004-2011 Jive Software. All rights reserved.
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
package org.jivesoftware.sparkimpl.plugin.privacy;

import java.awt.event.MouseEvent;
import java.util.TimerTask;


import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jivesoftware.resource.Res;
import org.jivesoftware.resource.SparkRes;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.privacy.packet.PrivacyItem;
import org.jivesoftware.smackx.privacy.packet.PrivacyItem.Type;
import org.jivesoftware.spark.SparkManager;
import org.jivesoftware.spark.plugin.ContextMenuListener;
import org.jivesoftware.spark.plugin.Plugin;
import org.jivesoftware.spark.ui.ContactItem;
import org.jivesoftware.spark.util.TaskEngine;
import org.jivesoftware.spark.util.log.Log;
import org.jivesoftware.sparkimpl.plugin.privacy.list.SparkPrivacyList;

/**
 * This is Privacy plugin for Spark.
 * 
 * This plugin built using specification: XEP-0016: Privacy Lists
 * {@see http://xmpp.org/extensions/xep-0016.html}
 * 
 * @author Zolotarev Konstantin, Bergunde Holger
 */
public class PrivacyPlugin implements Plugin {

    @Override
    public void initialize() {
        TimerTask pManagerInstance = new TimerTask() {
			@Override
			public void run() {
				PrivacyManager.getInstance(); // Initiating PrivacyLists
	
		        TimerTask thread = new TimerTask() {
					@Override
					public void run() {
						addMenuItemToContactItems();
					}
				};
				TaskEngine.getInstance().schedule(thread, 500);
			}
		};
		TaskEngine.getInstance().schedule(pManagerInstance, 1000);
		 

    }

    @Override
    public void shutdown() {
        // @todo remove Privacy List
    }

    @Override
    public boolean canShutDown() {
        return false;
    }

    @Override
    public void uninstall() {

    }

    protected void addPrivacyListsToPresenceChange() {

    }

    // /**
    // * Adding block menu item to contact popupmenu
    // */
    protected void addMenuItemToContactItems() {
        if (PrivacyManager.getInstance().isPrivacyActive()) {
            SparkManager.getContactList().addContextMenuListener(new ContextMenuListener() {
                @Override
                public void poppingUp(Object object, JPopupMenu popup) {

                    if (object instanceof ContactItem) {
                        final PrivacyManager pManager = PrivacyManager.getInstance();

                        if (pManager.hasActiveList()) {
                            final SparkPrivacyList activeList = pManager.getActiveList();

                            final ContactItem item = (ContactItem) object;
                            JMenuItem blockMenu;

                            if (activeList.isBlockedItem(item.getJID())) {
                                blockMenu = new JMenuItem(Res.getString("menuitem.unblock.contact"), SparkRes.getImageIcon(SparkRes.UNBLOCK_CONTACT_16x16));
                                blockMenu.addActionListener( ae -> {
                                    if (item != null) {
                                        try
                                        {
                                            activeList.removeItem( item.getJID());
                                            activeList.save();
                                        }
                                        catch ( SmackException.NotConnectedException e )
                                        {
                                            Log.warning( "Unable to remove item from block list: " + item, e );
                                        }
                                    }
                                } );
                            } else {
                                blockMenu = new JMenuItem(Res.getString("menuitem.block.contact"), SparkRes.getImageIcon(SparkRes.BLOCK_CONTACT_16x16));
                                blockMenu.addActionListener( ae -> {
                                    if (item != null) {
                                        PrivacyItem pItem = new PrivacyItem(Type.jid, item.getJID(), false, activeList.getNewItemOrder());
                                        pItem.setFilterMessage(true);
                                        pItem.setFilterPresenceOut(true);

                                        try
                                        {
                                            activeList.addItem(pItem);
                                            activeList.save();
                                        }
                                        catch ( SmackException.NotConnectedException e )
                                        {
                                            Log.warning( "Unable to add item to block list: " + item, e );
                                        }
                                    }
                                } );
                            }

                            popup.add(blockMenu);
                        }
                    }
                }

                @Override
                public void poppingDown(JPopupMenu popup) {
                    // ignore
                }

                @Override
                public boolean handleDefaultAction(MouseEvent e) {
                    return false;
                }
            });
        }

    }

}
