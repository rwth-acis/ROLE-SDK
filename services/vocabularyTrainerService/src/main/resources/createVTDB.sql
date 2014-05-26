-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.1.38-community


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema vocabularytrainer
--
DROP database vocabularytrainer;

CREATE DATABASE IF NOT EXISTS vocabularytrainer;
USE vocabularytrainer;

DROP TABLE IF EXISTS `challances`;
CREATE TABLE `challances` (
  `challanceid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `userid` int(10) unsigned NOT NULL,
  `challangerid` int(10) unsigned NOT NULL,
  `listid` int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (`challanceid`) --USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40000 ALTER TABLE `challances` DISABLE KEYS */;
/*!40000 ALTER TABLE `challances` ENABLE KEYS */;


DROP TABLE IF EXISTS `item_contexts`;
CREATE TABLE `item_contexts` (
  `itemid` int(10) unsigned NOT NULL,
  `context` text NOT NULL,
  `source` text NOT NULL,
  `contextid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`contextid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

/*!40000 ALTER TABLE `item_contexts` DISABLE KEYS */;
/*!40000 ALTER TABLE `item_contexts` ENABLE KEYS */;


DROP TABLE IF EXISTS `item_images`;
CREATE TABLE `item_images` (
  `itemid` int(10) unsigned NOT NULL,
  `imageurl` text NOT NULL,
  `imageid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`imageid`) --USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40000 ALTER TABLE `item_images` DISABLE KEYS */;
/*!40000 ALTER TABLE `item_images` ENABLE KEYS */;


DROP TABLE IF EXISTS `item_translations`;
CREATE TABLE `item_translations` (
  `itemid` int(10) unsigned NOT NULL,
  `translation` text NOT NULL,
  `translationid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`translationid`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

/*!40000 ALTER TABLE `item_translations` DISABLE KEYS */;
/*!40000 ALTER TABLE `item_translations` ENABLE KEYS */;


DROP TABLE IF EXISTS `items`;
CREATE TABLE `items` (
  `itemId` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `term` text NOT NULL,
  PRIMARY KEY (`itemId`)
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8;

/*!40000 ALTER TABLE `items` DISABLE KEYS */;
/*!40000 ALTER TABLE `items` ENABLE KEYS */;


DROP TABLE IF EXISTS `list_itemmap`;
CREATE TABLE `list_itemmap` (
  `listid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `itemid` int(10) unsigned NOT NULL,
  PRIMARY KEY (`listid`,`itemid`) --USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8;

/*!40000 ALTER TABLE `list_itemmap` DISABLE KEYS */;
/*!40000 ALTER TABLE `list_itemmap` ENABLE KEYS */;


DROP TABLE IF EXISTS `lists`;
CREATE TABLE `lists` (
  `listid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `listname` varchar(45) NOT NULL,
  `sourcelanguage` varchar(45) NOT NULL,
  `targetlanguage` varchar(45) NOT NULL,
  PRIMARY KEY (`listid`) --USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8;

/*!40000 ALTER TABLE `lists` DISABLE KEYS */;
/*!40000 ALTER TABLE `lists` ENABLE KEYS */;


DROP TABLE IF EXISTS `user_itemmap`;
CREATE TABLE `user_itemmap` (
  `userid` int(10) unsigned NOT NULL,
  `itemid` int(10) unsigned NOT NULL,
  `bucketnumber` int(10) unsigned NOT NULL,
  `correctanswers` int(10) unsigned NOT NULL,
  `wronganswers` int(10) unsigned NOT NULL,
  PRIMARY KEY (`userid`,`itemid`) --USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40000 ALTER TABLE `user_itemmap` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_itemmap` ENABLE KEYS */;


DROP TABLE IF EXISTS `user_listmap`;
CREATE TABLE `user_listmap` (
  `userid` int(10) unsigned NOT NULL,
  `listid` int(10) unsigned NOT NULL,
  PRIMARY KEY (`userid`,`listid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40000 ALTER TABLE `user_listmap` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_listmap` ENABLE KEYS */;


DROP TABLE IF EXISTS `user_listscores`;
CREATE TABLE `user_listscores` (
  `userid` int(10) unsigned NOT NULL,
  `listid` int(10) unsigned NOT NULL,
  `bucket1correctanswers` int(10) unsigned NOT NULL,
  `bucket1wronganswers` int(10) unsigned NOT NULL,
  `bucket2correctanswers` int(10) unsigned NOT NULL,
  `bucket2wronganswers` int(10) unsigned NOT NULL,
  `bucket3correctanswers` int(10) unsigned NOT NULL,
  `bucket3wronganswers` int(10) unsigned NOT NULL,
  `bucket4correctanswers` int(10) unsigned NOT NULL,
  `bucket4wronganswers` int(10) unsigned NOT NULL,
  `bucket5correctanswers` int(10) unsigned NOT NULL,
  `bucket5wronganswers` int(10) unsigned NOT NULL,
  PRIMARY KEY (`userid`,`listid`) --USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40000 ALTER TABLE `user_listscores` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_listscores` ENABLE KEYS */;


DROP TABLE IF EXISTS `user_scores`;
CREATE TABLE `user_scores` (
  `userid` int(10) unsigned NOT NULL,
  `correctanswers` int(10) unsigned NOT NULL,
  `wronganswers` int(10) unsigned NOT NULL,
  `hoursoftraining` double unsigned NOT NULL,
  PRIMARY KEY (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40000 ALTER TABLE `user_scores` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_scores` ENABLE KEYS */;


DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `userid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `password` varchar(40) NOT NULL,
  `email` varchar(45) NOT NULL,
  PRIMARY KEY (`userid`) --USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;

/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;




/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
