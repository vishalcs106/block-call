package com.android.internal.telephony;

/**
 * Created by Admin on 17-06-2015.
 */
public interface ITelephony {
    boolean endCall();
    void answerRingingCall();
    void silenceRinger();
}
