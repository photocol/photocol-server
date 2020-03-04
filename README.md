# photocol-server
Java backend for the photocol.Photocol app

### Installation instructions

#### Database setup
This server requires [MariaDB][1] to be installed and running.

Log into MariaDB:

```bash
mysql -u root -p
```

Grant the correct permissions to allow the server the ability to access the DB:

```sql
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost'
IDENTIFIED BY 'password' WITH GRANT OPTION;
```

Clone the [photocol-DB_SETUP][1] repo, sign into MariaDB (`mysql -u root -p` with password "password") and set up the tables:
```
source ./dbload.sql
source ./testcase.sql
```

(After changes in the database, call these after running `source ./deletall.sql`.)

[TODO: The MySQL permissions will be changed in the future to use a user other than root.]

#### S3 authentication
The `credentials` file must be saved to the file `~/.aws/credentials` in order for authentication in the S3 SDK to work.

#### Maven deps
```bash
mvn install
```

### Run instructions

```$xslt
mvn clean compile
```

Main class is `photocol.Photocol`.

[1]: https://wiki.archlinux.org/index.php/MariaDB
[2]: https://github.com/photocol/photocol-DB_SETUP