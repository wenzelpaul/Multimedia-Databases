package com.mmdb.importer;

import oracle.jdbc.driver.OracleDriver;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class is used for importing a folder of images into the oracle database
 */
public class ImageImporter {

    /**
     * This method opens a connection to the oracle database, creates a directory and an image table.
     * Then, all images in the given directory are scanned and added to the table as BFiles
     *
     * @param args the location of the image directory
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException {
        if (args.length == 1){
            File imgDir = new File(args[0]);
            DriverManager.registerDriver(new OracleDriver());
            Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl12c", "system", "oracle");

            Statement statement = connection.createStatement();
            System.out.println("createing directory: " + imgDir.toString());
            statement.execute("CREATE OR REPLACE DIRECTORY IMAGE_DIR AS '" +  imgDir.getAbsolutePath()+ "'");
            System.out.println("creating table IMAGES");
            statement.execute("BEGIN\n" +
                                    "   EXECUTE IMMEDIATE 'DROP TABLE IMAGES';\n" +
                                    "EXCEPTION\n" +
                                    "   WHEN OTHERS THEN\n" +
                                    "      IF SQLCODE != -942 THEN\n" +
                                    "         RAISE;\n" +
                                    "      END IF;\n" +
                                    "END;");
            statement.execute("CREATE TABLE IMAGES (image BFILE)");

            File[] listOfFiles = imgDir.listFiles();
            if (listOfFiles != null) {
                for (File file:listOfFiles) {
                    String extension = file.getName().substring(file.getName().lastIndexOf('.'), file.getName().length());
                    if (file.isFile() && extension.equals(".jpg")){
                        System.out.println("adding image: " + file.getName());
                        statement.execute("INSERT INTO IMAGES VALUES (bfilename ('IMAGE_DIR', '" + file.getName() + "'))");
                    }
                }
            }
            statement.close();
            connection.close();

        }


    }
}