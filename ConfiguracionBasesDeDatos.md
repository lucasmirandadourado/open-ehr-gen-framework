# MySQL #

  1. Descargar el driver para Java: http://dev.mysql.com/downloads/connector/j/3.0.html
  1. Copiar el jar descargado en ehrgen/lib
  1. Configurar la conexión a la base en [ehrgen/grails-app/conf/DataSource.groovy](https://code.google.com/p/open-ehr-gen-framework/source/browse/trunk/ehrgen/grails-app/conf/DataSource.groovy)

```
// Configuracion para utilizar MySQL
// Selecciona la base de datos oehr_dev
driverClassName = "com.mysql.jdbc.Driver"
url = "jdbc:mysql://localhost:3306/oehr_dev?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8"
username = "root"
password = "vertrigo"

// Reutilizar conecciones existentes con la base de datos 
pooled = true
         
// create-drop borra la base de datos y la genera de nuevo cada vez que se ejecuta la aplicacion
// Ver mas opciones en la seccion 3.3: http://grails.org/doc/1.0/guide/3.%20Configuration.html
dbCreate = "create-drop"
         
// Poner en true para loguear consultas SQL
loggingSql = false
```

Para soportar subir imágenes de más de 1MB, es necesario cambiar la configuración de MySQL (my.ini), de lo contrario, si se desea subir una imagen de más de 1MB se obtendrá una excepción de MySQL. Por ejemplo se puede poner un valor de 10MB:

```
max_allowed_packet = 10M
```


# Postgres #

  1. Descargar el driver para Java: http://jdbc.postgresql.org/download.html
  1. Copiar el jar descargado en ehrgen/lib
  1. Configurar la conexión a la base en [ehrgen/grails-app/conf/DataSource.groovy](https://code.google.com/p/open-ehr-gen-framework/source/browse/trunk/ehrgen/grails-app/conf/DataSource.groovy)

```
driverClassName = "org.postgresql.Driver"
url = "jdbc:postgresql://localhost:5432/ehrgen"
username = "postgres"
password = "postgres"
dialect = net.sf.hibernate.dialect.PostgreSQLDialect

// Reutilizar conecciones existentes con la base de datos 
pooled = true
         
// create-drop borra la base de datos y la genera de nuevo cada vez que se ejecuta la aplicacion
// Ver mas opciones en la seccion 3.3: http://grails.org/doc/1.0/guide/3.%20Configuration.html
dbCreate = "create-drop"
         
// Poner en true para loguear consultas SQL
loggingSql = false
```


# SQL Server #

```
Por Sergio Retamar
Probado con SQL Server 2000 y SQL Server 2005 Express
```

## 1 ##

Descargar el driver  de mssql  para Java: acá dejo dos enlaces,
[este](http://www.google.com/url?q=http://www.microsoft.com/downloads/info.aspx%3Fna%3D46%26SrcFamilyId%3DA737000D-68D0-4531-B65D-DA0F2A735707%26SrcDisplayLang%3Den%26u%3Dhttp%253a%252f%252fdownload.microsoft.com%252fdownload%252fD%252f6%252fA%252fD6A241AC-433E-4CD2-A1CE-50177E8428F0%252f1033%252fsqljdbc_3.0.1301.101_enu.exe&ei=9BRUTZ2DJ8O78gbCsNj_CA&sa=X&oi=unauthorizedredirect&ct=targetlink&ust=1297357820642607&usg=AFQjCNFDVPU-n3E-hRQ9dD-xzAXxknoSbQ) primero.

Debería arrancar directamente la descarga del archivo que contiene el
driver. Y si ese no funciona, [este](http://www.google.com/url?q=http://www.microsoft.com/downloads/en/details.aspx%3FFamilyID%3D%2520a737000d-68d0-4531-b65d-da0f2a735707%26displaylang%3Den&ei=IhVUTbntI4H-8AaHtsSICQ&sa=X&oi=unauthorizedredirect&ct=targetlink&ust=1297357866590492&usg=AFQjCNGVcLyFxQgvRIaBTFuv_5J4gFo8lQ), que tiene el link para la descarga (hay que elegir la descarga de:
1033\sqljdbc\_3.0.1301.101\_enu.exe).


## 2 ##

Una vez descargado extraer los archivos contenidos y ubicar sqljdbc4.jar y copiarlo en la carpeta lib dentro del home de grails
(\grails-1.1.1\lib).


## 3 ##

Configurar el archivo de acceso a datos de OpenEHR-Gen  para que
utilice mssql. Hay que ubicar el archivo DataSource.groovy  que  lo ubicamos entrando primero a la carpeta de OpenEHR-Gen y ahi dentro a  \grails-app\conf. Abrimos DataSource.groovy  y ubicamos primero la seccion dataSource para configurarla como sigue:

```
   dataSource {
      pooled = false
      driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
      username = "sa"
      password = "tupassword"
      def dialect=org.hibernate.dialect.SQLServerDialect.class;
   }

```

Luego dentro de la sección enviroments ubicamos la sección development
(porque al menos en principio utilizamos esta) y la dejamos como sigue:

```
   development {
      dataSource {
         dbCreate = "create-drop"
         url = "jdbc:sqlserver://localhost:1433;DatabaseName=oehr_dev"
      }
   }
```

Notar que donde dice localhost puede ir una dirección ip de donde esté
el server que no necesariamente es la misma máquina.


## 4 ##

Mapear los nombres de tablas y campos que no acepta el mssql por
nombres que no traigan problemas:

Para esto ubicamos el archivo Authorization.groovy que lo vamos a
encontrar dentro de grails-app\domain\authorization. En este debemos agregar lo siguiente dentro de la clase Authorization:

```
    static mapping = {
        table 'autorizaciones'
        user column:'usuario'
    }
```

Esto es para que cuando se cree la tabla no tome el nombre de tabla
Authorization y el campo user, sino los especificados, ya que
"Authorization" y "user" son palabras reservadas de mssql server y da
error.

Y luego hay que ubicar el archivo: DvText.groovy que se encuentra
dentro de : grails-app\domain\hce\core\data\_types\text
acá el problema es con la línea:

```
   maxSize :1024*1024*10
```

que define el largo de campo al crear la tabla en el sql, y como el
tipo de campo varchar acepta un máximo de 8000 caracteres no lo
consigue generar, en cambio  cambiando esa línea por:

```
   maxSize: 8000
```

Se puede arrancar todo sin error, igualmente habría que ver que consecuencias trae el achicar el tamaño máximo de este campo.


## 5 ##

Crear en el servidor SQL al que apuntamos en dataSource una base
de datos del mismo nombre que se configuró en el acceso a datos (oehr\_dev en este caso). La base de datos se crea vacía (solo create
database oehr\_dev) luego el framework crea las tablas.

Y listo arrancar OpenEHR-Gen con: grails run-app (estando ubicados dentro de la carpeta del OpenEHR-Gen )

Y con esto al arrancar ya nos creará dentro de la base de datos todas
las tablas del framework con registros en algunas de ellas.

Luego cada vez que arranquemos el framework con esta configuración del
DataSource.groovy nos va a borrar y generar todo de nuevo, pero si
queremos que los datos se conserven solo debemos cambiar dentro de la
sección development la entrada dbCreate para que en lugar de decir dbCreate="create-drop" diga dbCreate="update".



# Oracle #

TODO