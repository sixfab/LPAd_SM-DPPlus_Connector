package com.truphone.lpa.impl;


import com.truphone.lpa.ApduChannel;
import com.truphone.lpa.apdu.ApduUtils;
import com.truphone.rsp.dto.asn1.rspdefinitions.EnableProfileResponse;
import com.truphone.util.LogStub;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

class EnableProfileWorker {
    private static final Logger LOG = Logger.getLogger(EnableProfileWorker.class.getName());

    private final String iccid;
    private final ApduChannel apduChannel;

    EnableProfileWorker(String iccid, ApduChannel apduChannel) {

        this.iccid = iccid;
        this.apduChannel = apduChannel;
    }

    String run() {
        String eResponse = transmitEnableProfile();

        return convertEnableProfileResponse(eResponse);
    }

    private String convertEnableProfileResponse(String eResponse) {

        try {
            EnableProfileResponse enableProfileResponse = new EnableProfileResponse();
            InputStream is = new ByteArrayInputStream(Hex.decodeHex(eResponse.toCharArray()));

            enableProfileResponse.decode(is);

            if (LogStub.getInstance().isDebugEnabled()) {
                LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - Enable response: " + enableProfileResponse.toString());
            }

            if (LocalProfileAssistantImpl.PROFILE_RESULT_SUCESS.equals(enableProfileResponse.getEnableResult().toString())) {

                if (LogStub.getInstance().isDebugEnabled()) {
                    LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - iccid:" + iccid + " profile enabled successfully");
                }
                apduChannel.sendStatus();

            } else {
                LOG.info(LogStub.getInstance().getTag() + " - iccid: " + iccid + " profile not enabled");
            }

            return enableProfileResponse.getEnableResult().toString();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, LogStub.getInstance().getTag() + " - " + e.getMessage(), e);
            LOG.severe(LogStub.getInstance().getTag() + " - iccid: " + iccid + " profile failed to be enabled. message: " + e.getMessage());

            throw new RuntimeException("Unable to enable profile: " + iccid + ", response: " + eResponse);
        } catch (DecoderException e) {
            LOG.log(Level.SEVERE, LogStub.getInstance().getTag() + " - " + e.getMessage(), e);
            LOG.severe(LogStub.getInstance().getTag() + " - iccid: " + iccid + " profile failed to be enabled. Exception in Decoder:" + e.getMessage());

            throw new RuntimeException("Unable to enable profile: " + iccid + ", response: " + eResponse);
        }
    }

    private String transmitEnableProfile() {

        if (LogStub.getInstance().isDebugEnabled()) {
            LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - Enabling profile: " + iccid);
        }

        String apdu = ApduUtils.enableProfileApdu(iccid, LocalProfileAssistantImpl.TRIGGER_PROFILE_REFRESH);

        if (LogStub.getInstance().isDebugEnabled()) {
            LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - Enable profile APDU: " + apdu);
        }

        String eResponse = apduChannel.transmitAPDU(apdu);
        
        if (LogStub.getInstance().isDebugEnabled()) {
            LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - Enable profile response: " + eResponse);
        }

        return eResponse;
    }
}
