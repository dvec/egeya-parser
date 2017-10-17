import groovy.sql.Sql

import java.nio.file.Paths

final SQL_CONFIG_PATH = Paths.get("config/sql.xml").toAbsolutePath()
final TARGETS_PATH = Paths.get("config/targets.xml").toAbsolutePath()
final TABLE_NAME = "posts"

def sqlConfig = new XmlSlurper().parse(SQL_CONFIG_PATH.toFile())

def targets = new XmlSlurper().parse(TARGETS_PATH.toFile())

// Create database: CREATE TABLE IF NOT EXISTS %s (date CHAR(64), header CHAR(64), img CHAR(64), url CHAR(64) unique)

while (true) {
    def sql = Sql.newInstance(sqlConfig.getProperty("url").toString(), sqlConfig.getProperty("user").toString(),
            sqlConfig.getProperty("password").toString(), "org.postgresql.Driver")

    for (def url : targets.children()) {
        HTMLGetter getter = new HTMLGetter(url.toString())
        getter.loadHTML()
        def result = getter.perform()
        for (int i = 0; i < result.size(); i++) {
            sql.execute(String.format(
                    "INSERT INTO %s (date, header, img, url) VALUES ('%s', '%s', '%s', '%s') ON CONFLICT (url) DO NOTHING",
                    TABLE_NAME, result[i].date, result[i].header, result[i].img, result[i].url, result[i]))
        }
    }
    sql.close()
    sleep(100)
}