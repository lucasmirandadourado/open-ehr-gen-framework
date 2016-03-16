Te cuento como está hecha ahora la integración con el sistema de pacientes mediante IHE PDQ, y cuales son las ideas atrás:

  1. La clase que hay que modificar para integrar servicios demográficos externos es [PixPdqDemographicAccess.groovy](http://code.google.com/p/open-ehr-gen-framework/source/browse/trunk/open-ehr-gen/src/groovy/demographic/PixPdqDemographicAccess.groovy)
  1. En esa clase es donde se implementa la llamada a los Web Services de IHE PIX/PDQ especificados aquí: [IHE PIX-PDQ](http://www.ihe.net/Technical_Framework/upload/IHE_ITI_Suppl_PIX_PDQ_HL7v3_Rev2-1_TI_2010-08-10.pdf)
  1. El punto central es el método sendMessageToPDQServiceSOAP, donde se hace la llamada SOAP al servicio de búsqueda de datos demográficos (PDQ).
  1. Para llamar al servicio, no se necesita ninguna librería extra como el Groovy WSClient, que tiene algunos problemas. Incluso en el código se ven algunos comentarios informando de estos problemas.
  1. La llamada al Web Service se hace en la línea: def pdqResponse = svc.getOpenSIH\_0020\_0020Prototipo\_0020WebService\_0020PublisherPort().consultaPaciente(request)
  1. El código que implementa la llamada está [aquí](http://code.google.com/p/open-ehr-gen-framework/source/browse/#svn%2Ftrunk%2Fopen-ehr-gen%2Fsrc%2Fjava%2Forg%2Fopensih%2Fwebservices)
  1. Ese código es un proxy (intermediario) generado a partir del WSDL que define los servicios web que puedo invocar.
  1. La generación del proxy a partir del WSDL, se puede hacer con la herramienta [Apache CXF](http://cxf.apache.org/), que tiene un comando para ello.
  1. Colocar el código Java generado por Apache CXF en "src/java", borrando el código que hay en "src/java/org".
  1. Modificar la llamada al servicio en el punto 5, para llamar al nuevo proxy.
  1. Verificar que los formatos de los mensajes con correctos para su debido procesamiento.