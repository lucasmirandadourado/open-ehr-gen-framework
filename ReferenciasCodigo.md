# Textos de la ontología en vistas #

```
  ${archetype.ontology.constraintDefinitionsList.definitions.code} 
  ${archetype.ontology.constraintDefinitionsList.definitions.text}
  <br/>
```

# Textos codificados en vistas #

1. cCodePhrase se usa en DvCodedText y DvOrdinal

```
  // ojo cCodePhrase.terminologyId es org.openehr.rm.support.identification.TerminologyID
  // y yo implemente hce.core.support.identification.TerminologyID
  ${cCodePhrase.terminologyId.getClass()}
  <%
    def ctrm = CtrlTerminologia.getInstance()
    values = ctrm.getNombreTerminos( cCodePhrase.terminologyId.name )
    codes = ctrm.getCodigoTerminos( cCodePhrase.terminologyId.name )
  %>
```