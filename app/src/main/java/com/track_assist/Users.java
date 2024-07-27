package com.track_assist;

public class Users {
    String guideName,guideId, patName, patAddress,  patientReg,passReg;
    Users(String guideName,String  guideId, String patName, String patAddress, String patientReg,String passReg)
    {
        this.guideId = guideId;
        this.guideName = guideName;
        this.patName = patName;
        this.patAddress = patAddress;
        this.patientReg=patientReg;
        this.passReg = passReg;
    }

    public String getGuideName() {
        return guideName;
    }

    public void setGuideName(String guideName) {
        this.guideName = guideName;
    }

    public String getGuideId() {
        return guideId;
    }

    public void setGuideId(String guideId) {
        this.guideId = guideId;
    }

    public String getPatAddress() {
        return patAddress;
    }

    public void setPatAddress(String patAddress) {
        this.patAddress = patAddress;
    }

    public String getPatName() {
        return patName;
    }

    public void setPatName(String patName) {
        this.patName = patName;
    }

    public String getPatientReg() {
        return patientReg;
    }

    public void setPatientReg(String patientReg) {
        this.patientReg = patientReg;
    }

    public String getPassReg() {
        return passReg;
    }

    public void setPassReg(String passReg) {
        this.passReg = passReg;
    }
}
