En esta wiki se exponen ideas sobre la definición y manejo de layout (posicionamiento espacial) dentro de OPEN EHR-Gen




# Introduccción #

Actualmente Open EHR-Gen trabaja con un solo layout, definido en el propio software. La idea es que el framework soporte varios layout y que estos sean definidos en la base de conocimiento (al igual que Arquetipos y Templates) y a la hora de definir un Template se elija uno de estos layout para disponer espacialmente el contenido definido en el Template.


# Detalles #

A continuación se muestra un XML de ejemplo en el cual se ve como se podría definir un layout:

```
<layput>
  <id>id_layout_ejemplo</id>
  <name>nombre_layout_ejemplo</name>
  <pageZones>
    <row pos="1" area="25">
      <column id="top" pos="1" area="100" />
    </row>
    <row pos="2" area="50">
      <column id="left" pos="1" area="50" />
      <column id="right" pos="2" area="50" />
    </row>
    <row pos="3" area="25">
      <column id="bottom" pos="1" area="100" />
    </row>
  </pageZones>
  <ontology>
    <termDefinition idioma="ES">
      <term id="top">
        <name>Región Superior</name>
        <description>Los elementos contenidos en esta región serán visualizados en la parte superior de la pantalla
        </descripcion>
      </term>
      <term id="left">
        <name>Región Izquierda</name>
        <description>Los elementos contenidos en esta región serán visualizados en la parte izquierda de la pantalla
        </descripcion>
      </term>
      <term id="right">
        <name>Región Derecha</name>
        <description>Los elementos contenidos en esta región serán visualizados en la parte derecha de la pantalla</descripcion>
      </term>
      <term id="bottom">
        <name>Región Inferior</name>
        <description>Los elementos contenidos en esta región serán visualizados en la parte inferior de la pantalla
        </descripcion>
      </term>			
    </termDefinition>
    <termDefinition idioma="EN">
      <term id="top">
        <name>Superior Region</name>
        <description>The elements in this region will be displayed at the top of the screen
        </descripcion>
      </term>
      <term id="left">
        <name>Left Region</name>
        <description>The elements in this region will be displayed on the left side of the screen
        </descripcion>
      </term>
      <term id="right">
        <name>Right Region</name>
        <description>The elements in this region will be displayed on the right side of the screen
        </descripcion>
      </term>
      <term id="bottom">
        <name>Lower Region</name>
        <description>The elements in this region will be displayed in the bottom of the screen
        </descripcion>
      </term>			
    </termDefinition>
  </ontology>
</layput>
```