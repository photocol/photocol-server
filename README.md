# Photocol Server
Java backend for the photocol.Photocol app. The following instructions are to run the Photocol server standalone (i.e., for development). If you want to run the whole project configuration using `docker-compose` containers along with the rest of the project, see the instructions at [photocol-DB_SETUP][6].

### Installation instructions

###### Database setup
This server requires [MariaDB][1] to be installed and running.

Log into MariaDB as root user:

```bash
mysql -u root -p
```

Clone the [photocol-DB_SETUP][1] repo, sign into MariaDB (`mysql -u root -p` with password "password") and set up the tables. Run the scripts [`scripts/a.sql`][3] (to create the necessary tables) and [`scripts/b.sql`][4] (to set the proper permissions for the Java user) from this repo to set up the database.

This can be done after cloning the repository with:
```sql
source scripts/a.sql
source scripts/b.sql
```

###### S3 authentication
You must have an S3 bucket called `photocol` set up and the proper credentials to access it. The [`credentials`][5] file containing your S3 credentials. must be saved to the file `~/.aws/credentials` in order for authentication in the S3 SDK to work.

###### Maven deps
```bash
mvn install
```

### Run instructions

The environment variables `DB_URL` and `DB_PASSWORD` must be set to their respective values. With the default setup: `DB_URL=jdbc:mysql://localhost/photocol;DB_PASS=password`

To run:

```$xslt
mvn clean compile
```

The main class is `photocol.Photocol`.

[1]: https://wiki.archlinux.org/index.php/MariaDB
[2]: https://github.com/photocol/photocol-DB_SETUP
[3]: https://github.com/photocol/photocol-DB_SETUP/blob/master/script/a.sql
[4]: https://github.com/photocol/photocol-DB_SETUP/blob/master/script/b.sql
[5]: https://docs.aws.amazon.com/sdk-for-php/v3/developer-guide/guide_credentials_profiles.html
[6]: https://github.com/photocol/photocol-DB_SETUP