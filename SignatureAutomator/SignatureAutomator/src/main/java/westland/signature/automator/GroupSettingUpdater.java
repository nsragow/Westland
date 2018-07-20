package westland.signature.automator;

import com.google.api.services.groupssettings.model.Groups;

public class GroupSettingUpdater
{
  public static Groups setSettingsToManagement(Groups group)
  {
    group.setWhoCanPostMessage("ANYONE_CAN_POST");
    group.setShowInGroupDirectory("false");
    group.setIncludeInGlobalAddressList("false");

    return setGeneralSettings(group);
  }
  public static Groups setSettingsToStaff(Groups group)
  {
    group.setWhoCanPostMessage("ALL_MANAGERS_CAN_POST");
    group.setShowInGroupDirectory("true");
    group.setIncludeInGlobalAddressList("true");

    return setGeneralSettings(group);
  }
  private static Groups setGeneralSettings(Groups group)
  {
    group.setWhoCanJoin("INVITED_CAN_JOIN");
    group.setWhoCanViewMembership("ALL_IN_DOMAIN_CAN_VIEW");
    group.setWhoCanViewGroup("ALL_MEMBERS_CAN_VIEW");
    group.setWhoCanInvite("ALL_MANAGERS_CAN_INVITE");
    group.setWhoCanAdd("ALL_MANAGERS_CAN_ADD");
    group.setAllowExternalMembers("false");
    group.setAllowWebPosting("false");
    group.setMaxMessageBytes(26214400);
    group.setIsArchived("true");
    group.setArchiveOnly("false");
    group.setMessageModerationLevel("MODERATE_NONE");
    group.setSpamModerationLevel("ALLOW");
    group.setReplyTo("REPLY_TO_IGNORE");
    group.setIncludeCustomFooter("false");
    group.setSendMessageDenyNotification("false");
    group.setAllowGoogleCommunication("false");
    group.setMembersCanPostAsTheGroup("false");
    group.setMessageDisplayFont("DEFAULT_FONT");
    group.setWhoCanLeaveGroup("NONE_CAN_LEAVE");
    group.setWhoCanContactOwner("ALL_IN_DOMAIN_CAN_CONTACT");

    return group;
  }
}
