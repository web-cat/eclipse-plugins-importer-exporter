package org.webcat.eclipse.projectlink.exporter;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.webcat.eclipse.projectlink.exporter.messages"; //$NON-NLS-1$
	public static String SubmitAssignmentWizard_No_Response_Dialog_Title;
	public static String SubmitAssignmentWizard_No_Response_Message;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
