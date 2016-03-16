# Introduction #

Add your content here.


# DataValues #

La creación de DataValues se realiza 100% en la fábrica de objetos del RM, y se manejan objetos individuales: nunca se retorna una lista de objetos.

## Recepción de datos nulos o vacíos ##

Al recibir datos vacíos, la fábrica de objetos del RM retorna NULL.


## Recepción de datos parciales ##

Al recibir datos parciales, la fábrica hace crea el objeto, hace el binding y valida. Los errores que se generen deben ser los de las constraints del DataValue.

En el caso de que se espere recibir un dato numérico y se reciba un string, la fábrica hace la conversión del string a numérico y verifica errores de tipo (que el string no sea convertible a numérico). Ese error se inserta en los errores del objeto.


## Recepción de datos completos ##

Idem al caso anterior. Dependerá de las restricciones del DataValue si esos datos validan o no.