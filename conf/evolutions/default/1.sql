# create User and AuthToken

# --- !Ups

create table "User" (
  "userID" UUID NOT NULL PRIMARY KEY,
  "providerID" VARCHAR NOT NULL,
  "providerKey" VARCHAR NOT NULL,
  "firstName" VARCHAR,
  "lastName" VARCHAR,
  "fullName" VARCHAR,
  "email" VARCHAR,
  "activated" BOOLEAN NOT NULL,
  "hasher" VARCHAR NOT NULL,
  "password" VARCHAR NOT NULL,
  "salt" VARCHAR
);

create table "AuthToken" (
  "id" UUID  NOT NULL PRIMARY KEY,
  "userID" UUID NOT NULL,
  "expiry" TIMESTAMP NOT NULL
);

create table "CookieAuthenticator" (
  "id" VARCHAR NOT NULL PRIMARY KEY,
  "providerID" VARCHAR NOT NULL,
  "providerKey" VARCHAR NOT NULL,
  "lastUsedDateTime" TIMESTAMP NOT NULL,
  "expirationDateTime" TIMESTAMP NOT NULL,
  "idleTimeout" BIGINT,
  "cookieMaxAge" BIGINT,
  "fingerprint" VARCHAR
);

# --- !Downs

drop table "AuthToken";

drop table "User";

drop table "CookieAuthenticator";
