


# Introducción #

Esta interfaz permitirá ver a distintos sistemas de Historia Clínica Electrónica como un solo sistema integrado, tanto desde el punto de vista de los médicos como de los pacientes, y ofrecerá servicios a todos los actores del sistema de salud.

Contando con una interfaz de este tipo, sobre los sistemas basados en OpenEHR, podremos cruzar y consolidar información de distintos sistemas, de una forma "Orientada a Servicios", es decir, sin conocer previamente los detalles de la implementación de cada subsistema.

Esta es la única forma de conseguir una Historia Clínica Electrónica Única para cada paciente, sin considerar la alternativa de que todos los agentes en el sistema de salud utilicen el mismo sistema monolítico (improbable).


# Detalles #

Si bien la especificación del Modelo de Servicios (SM) de OpenEHR está en estado "draft", existen implementaciones que sirven como referencia.

  * http://www.openehr.org/wiki/display/spec/openEHR+Service+Model
  * http://www.openehr.org/wiki/display/spec/vEHR+Service+Specification
  * http://www.openehr.org/wiki/display/spec/Ocean+EhrGate+-+TDO+programming+example


# Ideas #

La implementación de esta interfaz puede estar basada y/o ayudar a perfilar los sistemas de HCE basados en OpenEHR según IHE XDS, PIX y PDQ (lo que permitiría una integración más simple con sistemas basados en mensajería HL7).

## API ##

```
// Interfaz para realizar busquedas en un EHR
Locatable query( EHRCriteria criteria, EHRResult result )

// Indica los filtros que deben pasar los elementos a buscar
EHRCriteria {
  List patients
  List responsibles
  Interval<DateTime> fromTo
}

// Indica que respuesta se quiere
EHRResult {
  List include // Dice que tipos del RM incluye (p.e. COMPOSITION, OBSERVATION, ... podrian ser conceptos concretos como ids de arquetipos)
  List exclude // Dice que tipos del RM excluye
}
```


## Estructura interna de registros de un EHR ##

El EHR debería poder soportar agregar documentación clínica en diversos formatos, además de que se tengan los datos estructurados en Compositions.

Entonces, del EHR de un paciente, se podrían tener asociadas:
  * Compositions
  * CDAs
  * CCRs
  * 13606
  * otros (pdfs, docs, etc)

Para que esto funcione, como los datos que se visualizarán no dependen del tipo de estructura en que esté la información, estos deben tener datos en común que permitan ordenar, buscar, filtrar registros, tales como:
  * fechas
  * tipo/clase
  * identificación del paciente
  * identificación del responsable
  * otros...