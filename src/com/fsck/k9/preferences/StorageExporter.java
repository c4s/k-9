package com.fsck.k9.preferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;

import android.app.Activity;
import android.util.Log;

import com.fsck.k9.K9;

public class StorageExporter {
    private static void exportPreferences(Activity activity, boolean includeGlobals, Set<String> accountUuids, String fileName, OutputStream os, String encryptionKey) throws StorageImportExportException  {
        try {
            IStorageExporter storageExporter = new StorageExporterEncryptedXml();
            if (storageExporter.needsKey() && encryptionKey == null) {
                throw new StorageImportExportException("Encryption key required, but none supplied");
            } else {
                finishExport(activity, storageExporter, includeGlobals, accountUuids, fileName, os, encryptionKey);
            }
        }
        catch (Exception e) {
            //FIXME: get this right
            throw new StorageImportExportException();
        }
    }

    public static void exportPreferences(Activity activity, boolean includeGlobals, Set<String> accountUuids, String fileName, String encryptionKey) throws StorageImportExportException {
        exportPreferences(activity, includeGlobals, accountUuids, fileName, null, encryptionKey);
    }

    private static void finishExport(Activity activity, IStorageExporter storageExporter, boolean includeGlobals, Set<String> accountUuids, String fileName, OutputStream os, String encryptionKey) throws StorageImportExportException {
        boolean needToClose = false;
        try {
            // This needs to be after the password prompt.  If the user cancels the password, we do not want
            // to create the file needlessly
            if (os == null && fileName != null) {
                needToClose = true;
                File outFile = new File(fileName);
                os = new FileOutputStream(outFile);
            }
            if (os != null) {

                OutputStreamWriter sw = new OutputStreamWriter(os);
                PrintWriter pf = new PrintWriter(sw);
                pf.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

                pf.println("<k9settings version=\"" + 1 + "\">");
                pf.flush();

                storageExporter.exportPreferences(activity, includeGlobals, accountUuids, os, encryptionKey);

                pf.println("</k9settings>");
                pf.flush();
            } else {
                throw new StorageImportExportException("Internal error; no fileName or OutputStream", null);
            }
        } catch (Exception e) {
            throw new StorageImportExportException(e.getLocalizedMessage(), e);
        } finally {
            if (needToClose && os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    Log.w(K9.LOG_TAG, "Unable to close OutputStream", e);
                }
            }
        }

    }

}
