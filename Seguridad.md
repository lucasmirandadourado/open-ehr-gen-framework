


# Introducción #

Para lograr incorporar una HCE basada en Open EHR-Gen en un ambiente de producción, es necesario disponer de mecanismos de alto nivel para la definición de permisos y la gestión de usuarios. Los permisos deberían ser administrables desde la aplicación (o desde una aplicación que utilice la misma DB). La granularidad debería ser lo más fina posible, y las condiciones de acceso deberían basarse en múltiples factores.


# Factores para reglas de seguridad #

Las reglas de seguridad deberían definirse en base a los siguientes factores:
  * Roles asignados al usuario (los roles de los usuarios en el sistema para el tema de autorización podrían ser distintos a los roles de los usuarios en la realidad)
  * Características del perfil del usuario (p.e. si es médico o es enfermera)
  * El usuario individual (permisos para ese usuario)
  * Estación de trabajo desde la que se accede:
    * Acceso desde dentro o fuera de una institución
    * Acceso desde una estación en un determinado departamento
    * Acceso desde una estación con su IP dentro de un rango dado

Para un usuario, se podrían definir un conjunto de reglas en base a estos factores, y esas reglas se verificarían según el contexto, escalando de reglas generales a reglas particulares en caso de cumplirse. Si una regla genérica no se cumple, no se escala a la siguiente regla.


# Trazabilidad y logging #

Este plugin de Grails está interesante para las tareas de logging: http://www.grails.org/plugin/audit-logging