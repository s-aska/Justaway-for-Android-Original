package info.justaway.listener;

/**
 * ふぁぼ、RT、ツイ消しした時にAdapterとかMainActivityのnotifyDataSetChangedを叩きまくりたいよね
 */
public interface StatusActionListener {

    // ふぁぼ、RT
    public void onStatusAction();

    // ツイ消し
    public void onRemoveStatus(long statusId);
}
