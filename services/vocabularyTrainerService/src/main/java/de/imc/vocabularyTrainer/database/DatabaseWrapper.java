package de.imc.vocabularyTrainer.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import de.imc.vocabularyTrainer.UserVIList;
import de.imc.vocabularyTrainer.UserVIListBucket;
import de.imc.vocabularyTrainer.VIList;
import de.imc.vocabularyTrainer.VItem;
import de.imc.vocabularyTrainer.VItemContext;
import de.imc.vocabularyTrainer.VItemImage;
import de.imc.vocabularyTrainer.VItemTranslation;
import de.imc.vocabularyTrainer.VTScore;
import de.imc.vocabularyTrainer.VTUser;

/**
 * Handles all connection to the database
 * @author Daniel Dahrendorf (daniel.dahrendorf@im-c.de)
 * @version 0.1, 21.12.2009
 */
public class DatabaseWrapper {

    private static Logger logger = Logger.getLogger(DatabaseWrapper.class);

    /**
     * The database connection
     */
    private DataSource dataSource;
    private String jdbcString;

    /**
     * Constructor
     * @param dataSource
     */
    // public DatabaseWrapper(DataSource dataSource) {
    // this.dataSource = dataSource;
    //		
    // }

    public DatabaseWrapper(String jdbcString) {

        this.jdbcString = jdbcString;

        Context initContext;

        try {
            initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            dataSource = (DataSource) envContext.lookup(jdbcString);

        } catch (NamingException e) {
            logger.error(e.toString());
        }

    }

    private Connection getConnection() throws SQLException {

        Connection connection = dataSource.getConnection();

        if (connection.isClosed()) {

            logger.debug("Found a closed connection. Try get a new one");

            Context initContext;

            try {

                initContext = new InitialContext();
                Context envContext = (Context) initContext.lookup("java:/comp/env");
                dataSource = (DataSource) envContext.lookup(jdbcString);
                connection = dataSource.getConnection();

            } catch (NamingException e) {
                logger.error(e.toString());
            }
        } else {
            logger.debug("Found an open connection.");
        }

        return connection;
    }

    /**
     * Gets the userId for a given userName
     * @param userName
     * @return
     */
    public int getUserId(String userName) {

        // prepare query
        String query = "SELECT userid FROM users as u WHERE username='" + userName + "';";

        int userid = 0;

        try {
            Connection connection = getConnection();

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                userid = rs.getInt("userid");
            }

            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            connection.close(); // Return to connection pool
            connection = null; // Make sure we don't close it twice

        } catch (SQLException e) {
            logger.error(e.toString());
        }

        return userid;
    }

    /**
     * Creates a new user in the db
     * @param user
     * @param password
     * @param firstList
     */
    public void createUser(VTUser user, String password, VIList firstList) {

        // prepare queries
        String createUserQuery = "INSERT INTO users (userid, username, password, email) " + "VALUES (null, '"
                + user.getUsername() + "', '" + password + "', " + "'" + user.getEmail() + "');";

        String createListQuery = "INSERT INTO lists (listid, listname, sourcelanguage, targetlanguage) "
                + "VALUES (null,'" + firstList.getListName() + "'," + "'" + firstList.getSourceLanguage() + "','"
                + firstList.getTargetLanguage() + "');";

        try {
            Connection connection = getConnection();

            // execute create user query and return userId
            Statement userStmt = connection.createStatement();
            userStmt.executeUpdate(createUserQuery, Statement.RETURN_GENERATED_KEYS);

            ResultSet userRes = userStmt.getGeneratedKeys();

            int userId = 0;
            while (userRes.next()) {
                userId = userRes.getInt(1);
            }

            // create scores for the user
            String createScoresQuery = "INSERT INTO user_scores (userid,correctanswers,wronganswers,hoursoftraining) "
                    + "VALUES (" + userId + ",0,0,0);";

            Statement scoreStmt = connection.createStatement();
            scoreStmt.executeUpdate(createScoresQuery);

            // execute create list query and return listId
            Statement listStmt = connection.createStatement();
            listStmt.executeUpdate(createListQuery, Statement.RETURN_GENERATED_KEYS);

            ResultSet listRes = listStmt.getGeneratedKeys();

            int listId = 0;
            while (listRes.next()) {
                listId = listRes.getInt(1);
            }

            String createListMapQuery = "INSERT INTO user_listmap (userid,listid) VALUES ('" + userId + "','" + listId
                    + "')";
            Statement lisMapStmt = connection.createStatement();
            lisMapStmt.executeUpdate(createListMapQuery);

            String createListScoresQuery = "INSERT INTO user_listscores (`userid`, `listid`, "
                    + "`bucket1correctanswers`, `bucket1wronganswers`, "
                    + "`bucket2correctanswers`, `bucket2wronganswers`, "
                    + "`bucket3correctanswers`, `bucket3wronganswers`, "
                    + "`bucket4correctanswers`, `bucket4wronganswers`, "
                    + "`bucket5correctanswers`, `bucket5wronganswers`) " + "VALUES ('" + userId + "','" + listId
                    + "','0','0','0','0','0','0','0','0','0','0');";
            Statement listScoreStmt = connection.createStatement();
            listScoreStmt.executeUpdate(createListScoresQuery);

            userRes.close();
            userRes = null;
            userStmt.close();
            userStmt = null;

            scoreStmt.close();
            scoreStmt = null;

            listStmt.close();
            listStmt = null;

            lisMapStmt.close();
            lisMapStmt = null;

            listScoreStmt.close();
            listScoreStmt = null;

            connection.close(); // Return to connection pool
            connection = null; // Make sure we don't close it twice

        } catch (SQLException e) {
            logger.error(e.toString());
        }

        logger.info("New user created: " + user.getUsername() + "[" + user.getUserId() + "]");
    }

    /**
     * Gets the user for the given userId
     * @param userId
     * @return
     */
    public VTUser fetchUser(int userId) {

        String query = "SELECT userid,username,email FROM users as u WHERE userid='" + userId + "';";

        VTUser user = null;

        try {
            Connection connection = getConnection();

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                user = new VTUser(rs.getInt("userid"), rs.getString("username"), rs.getString("email"));
            }

            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            connection.close(); // Return to connection pool
            connection = null; // Make sure we don't close it twice

        } catch (SQLException e) {
            logger.error(e.toString());
        }
        return user;
    }

    /**
     * Get all lists of a user
     * @param userId
     * @return
     */
    public List<UserVIList> getUserLists(int userId) {
        List<UserVIList> returnList = new ArrayList<UserVIList>();

        String getListsQuery = "SELECT l.listid, l.listname, l.sourcelanguage, l.targetlanguage, "
                + "IFNULL(noi.numberOfItems,0) as numberOfItems, "
                + "noiin.numberOfItemsBucket1, IFNULL(uls.bucket1correctanswers,0) as bucket1correctanswers, IFNULL(uls.bucket1wronganswers,0) as bucket1wronganswers, "
                + "noiin.numberOfItemsBucket2, IFNULL(uls.bucket2correctanswers,0) as bucket2correctanswers, IFNULL(uls.bucket2wronganswers,0) as bucket2wronganswers, "
                + "noiin.numberOfItemsBucket3, IFNULL(uls.bucket3correctanswers,0) as bucket3correctanswers, IFNULL(uls.bucket3wronganswers,0) as bucket3wronganswers, "
                + "noiin.numberOfItemsBucket4, IFNULL(uls.bucket4correctanswers,0) as bucket4correctanswers, IFNULL(uls.bucket4wronganswers,0) as bucket4wronganswers, "
                + "noiin.numberOfItemsBucket5, IFNULL(uls.bucket5correctanswers,0) as bucket5correctanswers, IFNULL(uls.bucket5wronganswers,0) as bucket5wronganswers "
                + "FROM lists as l " + "LEFT JOIN user_listmap as ulm ON l.listid = ulm.listid " + "LEFT JOIN "
                + "(SELECT lim.listid, COUNT(*) as numberOfItems FROM list_itemmap lim GROUP BY lim.listid) as noi "
                + "ON l.listid = noi.listid " + "LEFT JOIN "
                + "(SELECT * FROM user_listscores uls WHERE uls.userid = '"
                + userId
                + "') as uls "
                + "ON l.listid = uls.listid "
                + "LEFT JOIN "
                + "(SELECT l.listid, "
                + "IFNULL(x.numberOfItemsBucket1,0) as numberOfItemsBucket1, "
                + "IFNULL(x.numberOfItemsBucket2,0) as numberOfItemsBucket2, "
                + "IFNULL(x.numberOfItemsBucket3,0) as numberOfItemsBucket3, "
                + "IFNULL(x.numberOfItemsBucket4,0) as numberOfItemsBucket4, "
                + "IFNULL(x.numberOfItemsBucket5,0) as numberOfItemsBucket5 "
                + "FROM lists as l LEFT JOIN "
                + "(SELECT lim.listid, "
                + "IFNULL(SUM(IF(bucketnumber='1',1,0)),0) as numberOfItemsBucket1, "
                + "IFNULL(SUM(IF(bucketnumber='2',1,0)),0) as numberOfItemsBucket2, "
                + "IFNULL(SUM(IF(bucketnumber='3',1,0)),0) as numberOfItemsBucket3, "
                + "IFNULL(SUM(IF(bucketnumber='4',1,0)),0) as numberOfItemsBucket4, "
                + "IFNULL(SUM(IF(bucketnumber='5',1,0)),0) as numberOfItemsBucket5 "
                + "FROM list_itemmap as lim LEFT JOIN user_itemmap as uim ON lim.itemid = uim.itemid "
                + "GROUP BY lim.listid) as x ON x.listid = l.listid) as noiin "
                + "ON l.listid = noiin.listid "
                + "WHERE ulm.userid = '" + userId + "';";

        try {
            Connection connection = getConnection();

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(getListsQuery);

            while (rs.next()) {
                int listId = rs.getInt("listId");
                String listName = rs.getString("listName");
                String sourceLanguage = rs.getString("sourceLanguage");
                String targetLanguage = rs.getString("targetLanguage");
                int numberOfItems = rs.getInt("numberOfItems");

                int bucket1numberOfItems = rs.getInt("noiin.numberOfItemsBucket1");
                int bucket1correctanswers = rs.getInt("bucket1correctanswers");
                int bucket1wronganswers = rs.getInt("bucket1wronganswers");

                int bucket2numberOfItems = rs.getInt("noiin.numberOfItemsBucket2");
                int bucket2correctanswers = rs.getInt("bucket2correctanswers");
                int bucket2wronganswers = rs.getInt("bucket2wronganswers");

                int bucket3numberOfItems = rs.getInt("noiin.numberOfItemsBucket3");
                int bucket3correctanswers = rs.getInt("bucket3correctanswers");
                int bucket3wronganswers = rs.getInt("bucket3wronganswers");

                int bucket4numberOfItems = rs.getInt("noiin.numberOfItemsBucket4");
                int bucket4correctanswers = rs.getInt("bucket4correctanswers");
                int bucket4wronganswers = rs.getInt("bucket4wronganswers");

                int bucket5numberOfItems = rs.getInt("noiin.numberOfItemsBucket5");
                int bucket5correctanswers = rs.getInt("bucket5correctanswers");
                int bucket5wronganswers = rs.getInt("bucket5wronganswers");

                UserVIListBucket bucket1 = new UserVIListBucket(bucket1numberOfItems, bucket1correctanswers,
                        bucket1wronganswers);
                UserVIListBucket bucket2 = new UserVIListBucket(bucket2numberOfItems, bucket2correctanswers,
                        bucket2wronganswers);
                UserVIListBucket bucket3 = new UserVIListBucket(bucket3numberOfItems, bucket3correctanswers,
                        bucket3wronganswers);
                UserVIListBucket bucket4 = new UserVIListBucket(bucket4numberOfItems, bucket4correctanswers,
                        bucket4wronganswers);
                UserVIListBucket bucket5 = new UserVIListBucket(bucket5numberOfItems, bucket5correctanswers,
                        bucket5wronganswers);

                UserVIList tmpList = new UserVIList(listId, listName, sourceLanguage, targetLanguage, numberOfItems,
                        userId, bucket1, bucket2, bucket3, bucket4, bucket5);

                returnList.add(tmpList);
            }

            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            connection.close(); // Return to connection pool
            connection = null; // Make sure we don't close it twice

        } catch (SQLException e) {
            System.err.println("Error in DatabaseBaseWrapper:getUserLists: " + e.toString());
        }

        return returnList;
    }

    /**
     * Gets the item for a given list and user
     * @param listId
     * @param userId
     * @return
     */
    public List<List<VItem>> getUserItems(int listId, int userId) {
        List<List<VItem>> returnList = new ArrayList<List<VItem>>();

        List<VItem> bucketList1 = new ArrayList<VItem>();
        List<VItem> bucketList2 = new ArrayList<VItem>();
        List<VItem> bucketList3 = new ArrayList<VItem>();
        List<VItem> bucketList4 = new ArrayList<VItem>();
        List<VItem> bucketList5 = new ArrayList<VItem>();

        returnList.add(bucketList1);
        returnList.add(bucketList2);
        returnList.add(bucketList3);
        returnList.add(bucketList4);
        returnList.add(bucketList5);

        String itemQuery = "SELECT i.itemid, i.term, bn.bucketnumber FROM list_itemmap as lim "
                + "LEFT JOIN items as i ON lim.itemid = i.itemid " + "LEFT JOIN "
                + "(SELECT itemid, bucketnumber FROM user_itemmap u WHERE userid= '" + userId + "') as bn "
                + "ON lim.itemid = bn.itemid " + "WHERE listid = '" + listId + "';";

        Statement stmt;
        try {
            Connection connection = getConnection();

            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(itemQuery);

            while (rs.next()) {
                int itemId = rs.getInt("itemid");
                String term = rs.getString("term");
                int bucketnumber = rs.getInt("bucketnumber");

                // get the item contexts
                List<VItemContext> contextList = new ArrayList<VItemContext>();
                String contextQuery = "SELECT contextid, context, source FROM item_contexts " + "WHERE itemid = '"
                        + itemId + "';";

                Statement contextStmt = connection.createStatement();
                ResultSet contextRs = contextStmt.executeQuery(contextQuery);
                while (contextRs.next()) {
                    int contextId = contextRs.getInt("contextid");
                    String context = contextRs.getString("context");
                    String source = contextRs.getString("source");

                    contextList.add(new VItemContext(contextId, context, source));
                }

                // get the item translations
                List<VItemTranslation> translationList = new ArrayList<VItemTranslation>();
                String translationQuery = "SELECT translationid, translation FROM item_translations "
                        + "WHERE itemid = '" + itemId + "';";

                Statement translationStmt = connection.createStatement();
                ResultSet translationRs = translationStmt.executeQuery(translationQuery);
                while (translationRs.next()) {
                    int translationId = translationRs.getInt("translationid");
                    String translation = translationRs.getString("translation");

                    translationList.add(new VItemTranslation(translationId, translation));
                }

                // get the item images
                List<VItemImage> imageList = new ArrayList<VItemImage>();
                String imageQuery = "SELECT imageid, imageurl FROM item_images " + "WHERE itemid = '" + itemId + "';";

                Statement imageStmt = connection.createStatement();
                ResultSet imageRs = imageStmt.executeQuery(imageQuery);
                while (imageRs.next()) {
                    int imageId = imageRs.getInt("imageid");
                    String imageURL = imageRs.getString("imageurl");

                    imageList.add(new VItemImage(imageId, imageURL));
                }

                VItem tmpItem = new VItem(itemId, term, contextList, translationList, imageList);

                switch (bucketnumber) {
                case 1:
                    bucketList1.add(tmpItem);
                    break;
                case 2:
                    bucketList2.add(tmpItem);
                    break;
                case 3:
                    bucketList3.add(tmpItem);
                    break;
                case 4:
                    bucketList4.add(tmpItem);
                    break;
                case 5:
                    bucketList5.add(tmpItem);
                    break;
                default:
                    ;
                }

                contextStmt.close();
                contextStmt = null;
                contextRs.close();
                contextRs = null;

                translationStmt.close();
                translationStmt = null;
                translationRs.close();
                translationRs = null;

                imageStmt.close();
                imageStmt = null;
                imageRs.close();
                imageRs = null;
            }

            rs.close();
            rs = null;
            stmt.close();
            stmt = null;

            connection.close(); // Return to connection pool
            connection = null; // Make sure we don't close it twice

        } catch (SQLException e) {
            logger.error(e.toString());
        }
        return returnList;
    }

    /**
     * Deletes an item
     * @param itemId
     */
    public void deleteItem(int itemId) {

        try {
            Connection connection = getConnection();

            Statement stmt = connection.createStatement();

            String deleteItemFromItemsQuery = "DELETE FROM items " + "WHERE itemId = '" + itemId + "';";
            stmt.addBatch(deleteItemFromItemsQuery);

            String deleteItemFromContextQuery = "DELETE FROM item_contexts " + "WHERE itemId = '" + itemId + "';";
            stmt.addBatch(deleteItemFromContextQuery);

            String deleteItemFromtranslationsQuery = "DELETE FROM item_translations " + "WHERE itemId = '" + itemId
                    + "';";
            stmt.addBatch(deleteItemFromtranslationsQuery);

            String deleteItemFromImageQuery = "DELETE FROM item_images " + "WHERE itemId = '" + itemId + "';";
            stmt.addBatch(deleteItemFromImageQuery);

            String deleteItemFromListMapQuery = "DELETE FROM list_itemmap " + "WHERE itemId = '" + itemId + "';";
            stmt.addBatch(deleteItemFromListMapQuery);

            String deleteItemFromUserMapQuery = "DELETE FROM user_itemmap " + "WHERE itemId = '" + itemId + "';";
            stmt.addBatch(deleteItemFromUserMapQuery);

            stmt.executeBatch();

            stmt.close();
            stmt = null;

            connection.close(); // Return to connection pool
            connection = null; // Make sure we don't close it twice

        } catch (SQLException e) {
            logger.error(e.toString());
        }

    }

    /**
     * Updates a trained item for a user dependend on the training success of
     * the user
     * @param itemId 
     * @param userId
     * @param answerCorrect
     */
    public void itemTrained(int itemId, int userId, boolean answerCorrect) {

    	//prepare query for getting item information
        String userItemQuery = "SELECT UIM.itemId, listId, bucketnumber, correctanswers, wronganswers FROM user_itemmap as UIM, list_itemmap as LIM "
                + "WHERE UIM.itemId = LIM.itemId AND UIM.itemId = '" + itemId + "';";
        
        try {
        	
        	//prepare connection
            Connection connection = getConnection();

            //execute item information query
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(userItemQuery);

            if (rs.next()) {
                int bucketnumber = rs.getInt("bucketnumber");
                int listId = rs.getInt("itemId");

                String bucketRowToUpdate;
                String answerRowToUpdate;
                int newBucketNumber = bucketnumber;

                if (answerCorrect) {
                    if (bucketnumber < 5) {
                        newBucketNumber = bucketnumber + 1;
                    }
                    bucketRowToUpdate = "bucket" + bucketnumber + "correctanswers";
                    answerRowToUpdate = "correctanswers";

                } else {
                    if (bucketnumber > 1) {
                        newBucketNumber = bucketnumber - 1;
                    }
                    bucketRowToUpdate = "bucket" + bucketnumber + "wronganswers";
                    answerRowToUpdate = "wronganswers";
                }

                // prepare query to update the bucket information                
                String userItemUpdateQuery = "UPDATE user_itemmap " + "SET " + answerRowToUpdate + " = "
                        + answerRowToUpdate + " + 1, " + "bucketnumber = '" + newBucketNumber + "' "
                        + "WHERE itemid = '" + itemId + "' " + "AND userid = '" + userId + "';";

                stmt.executeUpdate(userItemUpdateQuery);

                // prepare query to update the user list score 
                String userListScoreUpdateQuery = "UPDATE user_listscores " + "SET " + bucketRowToUpdate + " = "
                        + bucketRowToUpdate + " + 1 " + "WHERE userid = '" + userId + "'" + "AND listid = '" + listId + "';";

                stmt.executeUpdate(userListScoreUpdateQuery);

             // prepare query to update the user overall score
                String userScoreUpdateQuery = "UPDATE user_scores " + "SET " + answerRowToUpdate + " = "
                        + answerRowToUpdate + " + 1 " + "WHERE userid = '" + userId + "';";

                stmt.executeUpdate(userScoreUpdateQuery);
            }

            rs.close();
            rs = null;
            stmt.close();
            stmt = null;

            connection.close(); // Return to connection pool
            connection = null; // Make sure we don't close it twice

        } catch (SQLException e) {
            logger.error(e.toString());
        }
    }

    /**
     * @param item
     * @param listId
     */
    public void createItem(VItem item, int listId) {

        // add item
        String addItemQuery;
        try {
            addItemQuery = "INSERT INTO items (itemid, term)" + "VALUES (null, '" + item.getTerm() + "');";

            Connection connection = getConnection();

            Statement stmt = connection.createStatement();
            stmt.executeUpdate(addItemQuery, Statement.RETURN_GENERATED_KEYS);

            ResultSet res = stmt.getGeneratedKeys();

            int itemId = 0;
            while (res.next()) {
                itemId = res.getInt(1);
            }

            Statement batchStmt = connection.createStatement();
            Iterator<VItemContext> iter = item.getContexts().iterator();

            while (iter.hasNext()) {
                VItemContext context = iter.next();

                String addContextQuery = "INSERT INTO item_contexts (contextid, itemid, context, source)"
                        + "VALUES (null, '" + itemId + "','" + context.getContext() + "','" + context.getSource()
                        + "');";

                batchStmt.addBatch(addContextQuery);
            }

            // add images
            Iterator<VItemImage> iter2 = item.getImages().iterator();

            while (iter2.hasNext()) {
                VItemImage image = iter2.next();

                String addImageQuery = "INSERT INTO item_images (imageid, itemid, imageurl)" + "VALUES (null, '"
                        + itemId + "','" + image.getImageURL() + "');";

                batchStmt.addBatch(addImageQuery);
            }

            // add translations
            Iterator<VItemTranslation> iter3 = item.getTranslations().iterator();
            while (iter3.hasNext()) {
                VItemTranslation translation = iter3.next();

                String addTranslationQuery = "INSERT INTO item_translations (translationid, itemid, translation)"
                        + "VALUES (null, '" + itemId + "','" + translation.getTranslation() + "');";

                batchStmt.addBatch(addTranslationQuery);
            }

            // add to list_itemmap
            String addToListItemMapQuery = "INSERT INTO list_itemmap (listid, itemid)" + "VALUES ('" + listId + "','"
                    + itemId + "');";

            batchStmt.addBatch(addToListItemMapQuery);

            // add item to all list user
            String addToUserItemMapQuery = "INSERT INTO user_itemmap (userid,itemid,bucketnumber,correctanswers,wronganswers) "
                    + "SELECT ulm.userid, '"
                    + itemId
                    + "', '1', '0', '0' FROM user_listmap AS ulm "
                    + "WHERE ulm.listid = '" + listId + "';";

            batchStmt.addBatch(addToUserItemMapQuery);

            // execute batch
            batchStmt.executeBatch();

            res.close();
            res = null;
            stmt.close();
            stmt = null;

            batchStmt.close();
            batchStmt = null;

            connection.close(); // Return to connection pool
            connection = null; // Make sure we don't close it twice

        } catch (SQLException e) {
            logger.error(e.toString());
        }

    }

    /**
     * Fetches the the score for a given userId
     * @param userId
     * @return
     */
    public VTScore fetchUserScore(int userId) {

        // prepare query
        String query = "SELECT userid, correctanswers, wronganswers  FROM user_scores " + "WHERE userid='" + userId
                + "';";

        VTScore score = null;

        try {
            // execute query

            Connection connection = getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                int correctAnswers = rs.getInt("correctanswers");
                int wrongAnswers = rs.getInt("wronganswers");

                double successRate = 0;

                // get the user lists and iterate sum the successrate
                List<UserVIList> userLists = getUserLists(userId);
                Iterator<UserVIList> iter = userLists.iterator();

                while (iter.hasNext()) {
                    successRate += iter.next().getSuccessRate();
                }

                // divide the successrate by the number of lists
                if (userLists.size() > 0) {
                    successRate = successRate / userLists.size();
                }

                score = new VTScore(userId, correctAnswers, wrongAnswers, successRate);
            }

            rs.close();
            rs = null;
            stmt.close();
            stmt = null;

            connection.close(); // Return to connection pool
            connection = null; // Make sure we don't close it twice

        } catch (SQLException e) {
            logger.error(e.toString());
        }

        return score;
    }

    /**
     * Gets the password for the given username
     * @param userName
     * @return
     */
    public char[] getPassword(String userName) {
        String query = "SELECT userid, password FROM users as u WHERE username='" + userName + "';";

        String password = "";

        try {
            Connection connection = getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                password = rs.getString("password");
            }

            rs.close();
            rs = null;
            stmt.close();
            stmt = null;

            connection.close(); // Return to connection pool
            connection = null; // Make sure we don't close it twice

        } catch (SQLException e) {
            logger.error(e.toString());
        }

        return password.toCharArray();
    }

    /**
     * Inserts a list in the database and add these list to the lists of the
     * user
     * @param list List which will be added to the database
     * @param userId User who created the list
     */
    public void createList(VIList list, int userId) {

        // prepare query
        String createListQuery = "INSERT INTO lists (listid, listname, sourcelanguage, targetlanguage) "
                + "VALUES (null,'" + list.getListName() + "','" + list.getSourceLanguage() + "','"
                + list.getTargetLanguage() + "');";

        try {

            // execute create list query and return listId
            Connection connection = getConnection();
            Statement listStmt = connection.createStatement();
            listStmt.executeUpdate(createListQuery, Statement.RETURN_GENERATED_KEYS);

            ResultSet listRes = listStmt.getGeneratedKeys();

            // get the listId of the created list
            int listId = 0;
            while (listRes.next()) {
                listId = listRes.getInt(1);
            }

            // create and execute the query for the user list map
            String createListMapQuery = "INSERT INTO user_listmap (userid,listid) VALUES ('" + userId + "','" + listId
                    + "')";

            Statement lisMapStmt = connection.createStatement();
            lisMapStmt.executeUpdate(createListMapQuery);

            // create and execute the query for the user list score
            String createListScoresQuery = "INSERT INTO user_listscores (`userid`, `listid`, "
                    + "`bucket1correctanswers`, `bucket1wronganswers`, "
                    + "`bucket2correctanswers`, `bucket2wronganswers`, "
                    + "`bucket3correctanswers`, `bucket3wronganswers`, "
                    + "`bucket4correctanswers`, `bucket4wronganswers`, "
                    + "`bucket5correctanswers`, `bucket5wronganswers`) " + "VALUES ('" + userId + "','" + listId
                    + "','0','0','0','0','0','0','0','0','0','0');";

            Statement listScoreStmt = connection.createStatement();
            listScoreStmt.executeUpdate(createListScoresQuery);

            listRes.close();
            listRes = null;
            listStmt.close();
            listStmt = null;
            lisMapStmt.close();
            lisMapStmt = null;
            listScoreStmt.close();
            listScoreStmt = null;

            connection.close(); // Return to connection pool
            connection = null; // Make sure we don't close it twice
        } catch (SQLException e) {
            logger.error(e.toString());
        }

    }

    /**
     * Deletes the list for the given listId
     * @param listId List which will be deleted
     */
    public void deleteList(int listId) {
        try {
            Connection connection = getConnection();
            Statement stmt = connection.createStatement();

            // prepare all queries
            String deleteListFromListsQuery = "DELETE FROM lists " + "WHERE listId = '" + listId + "';";
            stmt.addBatch(deleteListFromListsQuery);

            String deleteListFromUserListScoresQuery = "DELETE FROM user_listscores " + "WHERE listId = '" + listId
                    + "';";
            stmt.addBatch(deleteListFromUserListScoresQuery);

            String deleteListFromUserListMapQuery = "DELETE FROM user_listmap " + "WHERE listId = '" + listId + "';";
            stmt.addBatch(deleteListFromUserListMapQuery);

            String deleteListFromUserItemMapQuery = "DELETE FROM user_itemmap WHERE itemid IN "
                    + "(SELECT itemid FROM list_itemmap WHERE listid= '" + listId + "');";
            stmt.addBatch(deleteListFromUserItemMapQuery);

            String deleteListFromItemContextsQuery = "DELETE FROM item_contexts WHERE itemid IN "
                    + "(SELECT itemid FROM list_itemmap WHERE listid= '" + listId + "');";
            stmt.addBatch(deleteListFromItemContextsQuery);

            String deleteListFromItemImagesQuery = "DELETE FROM item_images WHERE itemid IN "
                    + "(SELECT itemid FROM list_itemmap WHERE listid= '" + listId + "');";
            stmt.addBatch(deleteListFromItemImagesQuery);

            String deleteListFromItemTranslationsQuery = "DELETE FROM item_translations WHERE itemid IN "
                    + "(SELECT itemid FROM list_itemmap WHERE listid= '" + listId + "');";
            stmt.addBatch(deleteListFromItemTranslationsQuery);

            String deleteListFromItemsQuery = "DELETE FROM items WHERE itemid IN "
                    + "(SELECT itemid FROM list_itemmap WHERE listid= '" + listId + "');";
            stmt.addBatch(deleteListFromItemsQuery);

            String deleteListFromListItemMapQuery = "DELETE FROM list_itemmap " + "WHERE listId = '" + listId + "';";
            stmt.addBatch(deleteListFromListItemMapQuery);

            // execute all queries
            stmt.executeBatch();

            stmt.close();
            stmt = null;

            connection.close(); // Return to connection pool
            connection = null; // Make sure we don't close it twice
        } catch (SQLException e) {
            logger.error(e.toString());
        }

    }
}
