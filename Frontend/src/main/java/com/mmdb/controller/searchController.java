package com.mmdb.controller;

import oracle.jdbc.*;
import oracle.sql.BFILE;
import org.apache.commons.io.FileUtils;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This RestController provides the results for the HTML file so that the user can find similar images to an uploaded one.
 *
 * @author Wenzel Pleyer, Caterina Rotondo, Christoph Stemp, Miriam Deml
 */
@RestController
public class searchController {

    /**
     * This method searches similar images for the uploaded image. The uploaded image and the 12 most similar images
     * will then be displayed.
     *
     * @param file the image to perform the similarity search on
     * @param model similar images get added to the model
     *
     * @return the index page as ModelAndView
     *
     * @throws SQLException throws a {@link SQLException}, if the connection to the database fails
     * @throws IOException throws an {@link IOException}
     */
    @PostMapping("/search")
    public ModelAndView search(@RequestParam MultipartFile file, Model model) throws SQLException, IOException {
        File fileName = new File("search.jpg");
        FileUtils.writeByteArrayToFile(fileName, file.getBytes());

        DriverManager.registerDriver(new OracleDriver());
        Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl12c",
                "system", "oracle");

        //perform the similarity search
        PreparedStatement statement =
                connection.prepareStatement("SELECT ROWID, COLUMN1 FROM IMG_TABLE WHERE similarity(COLUMN1, ?) > 0");
        statement.setString(1, fileName.getAbsolutePath());
        ResultSet resultSet = statement.executeQuery();

        //add the results to the resultSet
        List<String> rowids = new ArrayList<>();
        while (resultSet.next()) {
            rowids.add(resultSet.getString(1));
        }

        statement.close();
        connection.close();

        //load scores from score.txt file
        File scores = new File("scores.txt");
        String[] scoreArray = new String[12];
        if (scores.exists()){
            Scanner scanner = new Scanner(scores);
            for (int i = 0; i<scoreArray.length; i++){
                String line = scanner.nextLine();
                scoreArray[i] = "Score: " + String.valueOf(Math.round((100-Double.parseDouble(line.split(";")[1])))) + "%";
            }
        }

        model.addAttribute("rowids", rowids);
        model.addAttribute("searchImage", fileName.getAbsolutePath());
        model.addAttribute("scores", scoreArray);

        //stay on index page
        return new ModelAndView("index");
    }

    /**
     * This method fetches a similar image and stores it in the image folder
     *
     * @param id       the image to fetch
     * @param response the response that contains the image
     *
     * @throws SQLException throws a {@link SQLException}, if the connection to the database fails
     * @throws IOException throws an {@link IOException}
     */
    @GetMapping(value = "/image", produces = {"image/jpg"})
    public void getImage(@RequestParam String id, HttpServletResponse response) throws SQLException, IOException {

        DriverManager.registerDriver(new OracleDriver());
        Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl12c",
                "system", "oracle");
        PreparedStatement statement =
                connection.prepareStatement("SELECT COLUMN1 FROM IMG_TABLE WHERE ROWID = ?");
        statement.setString(1, id);

        OracleResultSet resultSet = (OracleResultSet) statement.executeQuery();
        if(resultSet!=null) {
            resultSet.next();

            BFILE bfile = resultSet.getBfile(1);
            bfile.openFile();

            response.setContentType("image/jpg");
            //copy image to response
            StreamUtils.copy(bfile.getBinaryStream(), response.getOutputStream());

            bfile.closeFile();
        }
        statement.close();
        connection.close();
    }

    /**
     * This method loads the searchfile and displays it on the website
     *
     * @param searchFile the path to the file on the disk
     * @param response the HttpServletResponse that contains the image
     *
     * @throws IOException throws an {@link IOException}
     */
    @GetMapping(value = "/searchImage", produces = ("image/jpg"))
    public void getSearchImage(@RequestParam String searchFile, HttpServletResponse response) throws IOException {
        response.setContentType("image/jpg");
        StreamUtils.copy(Files.readAllBytes(Paths.get(searchFile)), response.getOutputStream());
    }
}
