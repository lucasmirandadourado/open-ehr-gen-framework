# Introducción #

openEHR provee mecanismos que permiten, por un lado saber el estado de ejecución de una instrucción, y por otro, saber qué acciones están relacionadas con qué actividades.

Una instrucción puede tener una [máquina de estados](http://www.openehr.org/wiki/display/spec/Instructions+and+Actions+-+work+flow+in+openEHR) asociada, que permite trazar cómo avanzó la ejecución de una instrucción y cuál es su estado actual.

Cada instrucción tiene asociada un conjunto de actividades, y cada actividad referencia a un único arquetipo de tipo acción, que será el arquetipo que modela el registro del cumplimiento de una acción.

Cada acción tiene asociada el evento que causa una transición en la máquina de estados, ya que cuando se realiza una acción se puede cumplir una actividad de una instrucción. También se podría registrar que no se pudo cumplir la acción y porqué, en este caso la actividad no se cumple pero podría igual causar una transición del estado de la instrucción. _Con esto hay un potencial problema porque una acción no tiene asociadas varias transiciones, entonces creo que se toma la acción como evento en sí, sin considerar el modelo de la acción no cumplida, o no cumplida completamente, o no cumplida tal cual fue especificada en la instrucción_

Ver pag. 53 del ehr\_im.pdf

Herramientas útiles: http://www.openehr.org/svn/ref_impl_eiffel/TRUNK/apps/adl_workbench/doc/web/index.html

Sobre seguimiento del proceso clínico: http://www.openehr.org/wiki/display/impl/openEHR+RM+times+and+tracking+clinical+process