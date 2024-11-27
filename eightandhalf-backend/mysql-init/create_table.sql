
CREATE DATABASE /*!32312 IF NOT EXISTS*/`EightandHalf-user` /*!40100 DEFAULT CHARACTER SET utf8mb3 */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `EightandHalf-user`;


/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
                         `user_id` BIGINT NOT NULL,
                         `password` VARCHAR(20) NOT NULL,
                         `user_name` VARCHAR(20) NOT NULL,
                         `telephone` VARCHAR(11) NOT NULL,
                         `avatar_url` VARCHAR(127) DEFAULT NULL,
                         `created_at` DATETIME DEFAULT NULL,
                         `updated_at` DATETIME DEFAULT NULL,
                         `deleted_at` DATETIME DEFAULT NULL,
                         `is_singer` INT NOT NULL,
                         `singer_category` BIGINT DEFAULT NULL,
                         `secret` VARCHAR(255) DEFAULT NULL,
                         `qrcode` VARCHAR(255) DEFAULT NULL,
                         `identity` BIGINT DEFAULT NULL,
                         PRIMARY KEY (`user_id`),
                         KEY `user_name` (`user_name`),
                         KEY `identity` (`identity`),
                         CONSTRAINT `users_chk_1` CHECK ((`is_singer` IN (0,1)))
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `audit`;

/*Table structure for table `audit` */

CREATE TABLE `audit` (
                         `music_id` BIGINT NOT NULL,
                         `status` VARCHAR(127) NOT NULL,
                         `user_id` BIGINT DEFAULT NULL,
                         KEY `user_id` (`user_id`),
                         CONSTRAINT `audit_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `audit` */


CREATE DATABASE /*!32312 IF NOT EXISTS*/`EightandHalf-music` /*!40100 DEFAULT CHARACTER SET utf8mb3 COLLATE utf8mb3_bin */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `EightandHalf-music`;

/*Table structure for table `dictionary` */

DROP TABLE IF EXISTS `dictionary`;

CREATE TABLE `dictionary` (
                              `dic_id` BIGINT NOT NULL,
                              `type_code` VARCHAR(20) NOT NULL,
                              `value` VARCHAR(20) NOT NULL,
                              `order_no` INT NOT NULL,
                              PRIMARY KEY (`dic_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `dictionary` */

/*Table structure for table `music` */

DROP TABLE IF EXISTS `music`;

CREATE TABLE `music` (
                         `music_id` BIGINT NOT NULL,
                         `music_url` VARCHAR(127) NOT NULL,
                         `cover_url` VARCHAR(127) NOT NULL,
                         `user_id` BIGINT NOT NULL,
                         `author` VARCHAR(255) DEFAULT NULL,
                         `music_name` VARCHAR(127) DEFAULT NULL,
                         `description` VARCHAR(127) DEFAULT NULL,
                         `subscribe_count` INT DEFAULT NULL,
                         `comment_count` INT DEFAULT NULL,
                         `visit_count` INT DEFAULT NULL,
                         `music_category` BIGINT DEFAULT NULL,
                         `created_at` DATETIME DEFAULT NULL,
                         `updated_at` DATETIME DEFAULT NULL,
                         `deleted_at` DATETIME DEFAULT NULL,
                         PRIMARY KEY (`music_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `music` */


/*Table structure for table `playlists` */

DROP TABLE IF EXISTS `playlists`;

CREATE TABLE `playlists` (
                             `playlist_id` BIGINT NOT NULL,
                             `playlist_name` VARCHAR(127) DEFAULT NULL,
                             `playlist_cover_url` VARCHAR(127) NOT NULL,
                             `playlist_type` INT NOT NULL,
                             `description` VARCHAR(127) DEFAULT NULL,
                             `created_at` DATETIME DEFAULT NULL,
                             `updated_at` DATETIME DEFAULT NULL,
                             `deleted_at` DATETIME DEFAULT NULL,
                             PRIMARY KEY (`playlist_id`),
                             CONSTRAINT `playlists_chk_1` CHECK ((`playlist_type` IN (0,1)))
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `playlists` */


/*Table structure for table `playlist_music` */

DROP TABLE IF EXISTS `playlist_music`;

CREATE TABLE `playlist_music` (
                                  `playlist_id` BIGINT NOT NULL,
                                  `music_id` BIGINT NOT NULL,
                                  KEY `playlist_id` (`playlist_id`),
                                  KEY `music_id` (`music_id`),
                                  CONSTRAINT `playlist_music_ibfk_1` FOREIGN KEY (`playlist_id`) REFERENCES `playlists` (`playlist_id`),
                                  CONSTRAINT `playlist_music_ibfk_2` FOREIGN KEY (`music_id`) REFERENCES `music` (`music_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `playlist_music` */

/*Table structure for table `user_playlists` */

DROP TABLE IF EXISTS `user_playlists`;

CREATE TABLE `user_playlists` (
                                  `playlist_id` BIGINT NOT NULL,
                                  `user_id` BIGINT NOT NULL,
                                  `relationship_type` TINYINT(1) NOT NULL,
                                  `is_public` TINYINT(1) NOT NULL,
                                  KEY `playlist_id` (`playlist_id`),
                                  CONSTRAINT `user_playlists_ibfk_1` FOREIGN KEY (`playlist_id`) REFERENCES `playlists` (`playlist_id`),
                                  CONSTRAINT `user_playlists_chk_1` CHECK ((`relationship_type` IN (0,1))),
                                  CONSTRAINT `user_playlists_chk_2` CHECK ((`is_public` IN (0,1)))
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE DATABASE /*!32312 IF NOT EXISTS*/`EightandHalf-chat` /*!40100 DEFAULT CHARACTER SET utf8mb3 COLLATE utf8mb3_bin */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `EightandHalf-chat`;

/*Table structure for table `blacklists` */

DROP TABLE IF EXISTS `blacklists`;

CREATE TABLE `blacklists` (
                              `user_id` BIGINT NOT NULL,
                              `block_user_id` BIGINT NOT NULL
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `blacklists` */

/*Table structure for table `chat_messages` */

DROP TABLE IF EXISTS `chat_messages`;

CREATE TABLE `chat_messages` (
                                 `message_id` BIGINT NOT NULL,
                                 `user_id` BIGINT NOT NULL,
                                 `to_user_id` BIGINT DEFAULT NULL,
                                 `message_content` VARCHAR(255) DEFAULT NULL,
                                 `created_at` DATETIME DEFAULT NULL,
                                 `updated_at` DATETIME DEFAULT NULL,
                                 `deleted_at` DATETIME DEFAULT NULL,
                                 `is_transfer_music` INT NOT NULL,
                                 PRIMARY KEY (`message_id`),
                                 CONSTRAINT `chat_messages_chk_1` CHECK ((`is_transfer_music` IN (0,1)))
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE DATABASE /*!32312 IF NOT EXISTS*/`EightandHalf-community` /*!40100 DEFAULT CHARACTER SET utf8mb3 COLLATE utf8mb3_bin */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `EightandHalf-community`;

/*Table structure for table `blogs` */

DROP TABLE IF EXISTS `blogs`;

CREATE TABLE `blogs` (
                         `blog_id` BIGINT NOT NULL,
                         `user_id` BIGINT NOT NULL,
                         `like_count` INT DEFAULT NULL,
                         `blog_content` VARCHAR(255) DEFAULT NULL,
                         `picture_url` VARCHAR(255) DEFAULT NULL,
                         `music_id` BIGINT DEFAULT NULL,
                         `comment_count` INT DEFAULT NULL,
                         `created_at` DATETIME DEFAULT NULL,
                         `updated_at` DATETIME DEFAULT NULL,
                         `deleted_at` DATETIME DEFAULT NULL,
                         PRIMARY KEY (`blog_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `blogs` */

/*Table structure for table `comments` */

DROP TABLE IF EXISTS `comments`;

CREATE TABLE `comments` (
                            `comment_id` BIGINT NOT NULL,
                            `user_id` BIGINT NOT NULL,
                            `music_id` BIGINT NOT NULL,
                            `parent_id` BIGINT DEFAULT NULL,
                            `like_count` INT DEFAULT NULL,
                            `child_count` INT DEFAULT NULL,
                            `comment_content` VARCHAR(255) DEFAULT NULL,
                            `comment_count` INT DEFAULT NULL,
                            `created_at` DATETIME DEFAULT NULL,
                            `updated_at` DATETIME DEFAULT NULL,
                            `deleted_at` DATETIME DEFAULT NULL,
                            PRIMARY KEY (`comment_id`),
                            KEY `music_id` (`music_id`),
                            KEY `parent_id` (`parent_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `comments` */

/*Table structure for table `followers` */

DROP TABLE IF EXISTS `followers`;

CREATE TABLE `followers` (
                             `user_id` BIGINT NOT NULL,
                             `follower_user_id` BIGINT NOT NULL
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `followers` */

/*Table structure for table `likes` */

DROP TABLE IF EXISTS `likes`;

CREATE TABLE `likes` (
                         `user_id` BIGINT NOT NULL,
                         `comment_id` BIGINT DEFAULT NULL,
                         `blog_id` BIGINT DEFAULT NULL,
                         KEY `comment_id` (`comment_id`),
                         KEY `blog_id` (`blog_id`),
                         CONSTRAINT `likes_ibfk_1` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`comment_id`),
                         CONSTRAINT `likes_ibfk_2` FOREIGN KEY (`blog_id`) REFERENCES `blogs` (`blog_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

