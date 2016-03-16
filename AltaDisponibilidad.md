


# Introducción #

Los sistemas de información en salud pueden considerarse sistemas críticos, los cuales deben garantizar alta disponibilidad, y comportamiento robusto ante fallas y situaciones excepcionales.


## Búsqueda de servicios alternativos ##

Ningún servicio es infalible, los equipos se rompen, los servicios se caen, etc.

Una idea para tener un comportamiento robusto en estos casos es soportar "servicios alternativos", por ejemplo para la base de datos clínicos (CDR) y para la consulta de información demográfica (IMP). De esta forma si la consulta a un servicio falla, tener una lista de servicios alternativos que pueden servir de soporte mientras no se solucione la falla en el servicio original. De esta forma, si se detectan problemas en servicios usados por el sistema de HCEs, el sistema escala automáticamente a un servicio alternativo, permitiendo la continuidad del trabajo de los usuarios. Esto debería ser transparente para el usuario final.

Un servicio alternativo para la base de datos puede ser un respaldo de la base hecho en otro equipo. Para el IMP puede ser algo similar.


## Redundancia del sistema ##

Si falla el servidor donde está corriendo el sistema de HCE es imposible que la HCE implemente paliativos al problema. La solución en este caso es tener un servidor alternativo, réplica del sistema original, que permita escalar automáticamente cuando el servidor original falle. Esta estrategia de tener sistemas redundantes y autoescalar puede implementarse de varias formas. Una forma sencilla es mediante un proxy, que detecte la falla y redirija los pedidos hacia otro servidor. Igualmente si falla el proxy, se tendrían los mismos problemas. La ventaja es que teniendo un proxy se podrían tener los sistemas redundantes funcionando al mismo momento, y este proxy podría hacer balanceo de la carga.
Otra alternativa es mediante un servidor DNS. Por ejemplo, si se detecta una falla, se modificaría la regla de mapeo entre nombre de servidor e IP, por la IP del equipo con el sistema replicado, entonces también se redirige el pedido a este servidor hasta que se arregle el problema en el servidor original.


## Tolerancia a fallos ##

El sistema debería ser tolerante a errores de configuración (verificándolos, corrigiéndolos y notificando de posibles problemas).

Por ejemplo, el sistema debería soportar un arquetipo o template mal formado, es decir, que no lo carga, pero tampoco arroja un error interno del sistema al usuario, si un mensaje amigable. Pero también debería notificar la incidencia a un administrador del sistema (p.e. hacer un log y que el administrador tenga un listado de incidencias).

También ante errores de configuración (Config.groovy), el sistema debería comportarse de forma robusta. El sistema podría corregir estos problemas adoptando convenciones por defecto (p.e. si no se puede escribir en el directorio configurado para almacenar los CDAs, crear un directorio por defecto con los permisos para poder escribir, de no poder escribir, devolver un mensaje amigable al usuario y notificación al administrador).