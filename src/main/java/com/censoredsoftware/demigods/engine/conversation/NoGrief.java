package com.censoredsoftware.demigods.engine.conversation;

import org.bukkit.event.Listener;

import com.censoredsoftware.core.bukkit.ListedConversation;

public class NoGrief implements ListedConversation
{
	// Define variables
	private static org.bukkit.conversations.Conversation noGriefConversation;

	@Override
	public Listener getUniqueListener()
	{
		return new NoGriefListener();
	}

	// TODO Everything.
}

class NoGriefListener implements Listener
{
	// TODO Everything.
}
