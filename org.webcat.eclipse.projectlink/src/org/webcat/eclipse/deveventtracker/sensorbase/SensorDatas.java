//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.06.27 at 11:37:10 AM GMT-10:00 
//


package org.webcat.eclipse.deveventtracker.sensorbase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class SensorDatas
    implements Serializable
{

    private final static long serialVersionUID = 12343L;
    protected List<SensorData> sensorData;

    /**
     * Gets the value of the sensorData property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sensorData property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSensorData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SensorData }
     * 
     * 
     */
    public List<SensorData> getSensorData() {
        if (sensorData == null) {
            sensorData = new ArrayList<SensorData>();
        }
        return this.sensorData;
    }

    public boolean isSetSensorData() {
        return ((this.sensorData!= null)&&(!this.sensorData.isEmpty()));
    }

    public void unsetSensorData() {
        this.sensorData = null;
    }

	public String getFileString() {
		String representation = "";
		for (SensorData data : this.sensorData) 
		{
			representation += data.getFileString();
		}
		return representation;		
	}

}
