<!DOCTYPE qgis PUBLIC 'http://mrcc.com/qgis.dtd' 'SYSTEM'>
<qgis styleCategories="AllStyleCategories" simplifyLocal="1" hasScaleBasedVisibilityFlag="0" maxScale="0" simplifyAlgorithm="0" simplifyDrawingTol="1" simplifyMaxScale="1" simplifyDrawingHints="0" readOnly="0" minScale="1e+08" labelsEnabled="0" version="3.4.5-Madeira">
  <flags>
    <Identifiable>1</Identifiable>
    <Removable>1</Removable>
    <Searchable>1</Searchable>
  </flags>
  <renderer-v2 symbollevels="0" type="RuleRenderer" forceraster="0" enableorderby="0">
    <rules key="{5926142d-eb7f-4035-817c-284bc8a0d327}">
      <rule label="Grün- und Freihaltezone innerhalb Bauzone" key="{61fc9075-c807-49df-b6cd-1a4495932e94}" symbol="0" filter="&quot;artcode&quot;='N111'"/>
    </rules>
    <symbols>
      <symbol alpha="1" type="fill" clip_to_extent="1" force_rhr="0" name="0">
        <layer locked="0" enabled="1" class="SimpleFill" pass="0">
          <prop k="border_width_map_unit_scale" v="3x:0,0,0,0,0,0"/>
          <prop k="color" v="128,255,51,255"/>
          <prop k="joinstyle" v="bevel"/>
          <prop k="offset" v="0,0"/>
          <prop k="offset_map_unit_scale" v="3x:0,0,0,0,0,0"/>
          <prop k="offset_unit" v="MM"/>
          <prop k="outline_color" v="0,0,0,255"/>
          <prop k="outline_style" v="no"/>
          <prop k="outline_width" v="0.26"/>
          <prop k="outline_width_unit" v="MM"/>
          <prop k="style" v="solid"/>
          <data_defined_properties>
            <Option type="Map">
              <Option value="" type="QString" name="name"/>
              <Option name="properties"/>
              <Option value="collection" type="QString" name="type"/>
            </Option>
          </data_defined_properties>
        </layer>
      </symbol>
    </symbols>
  </renderer-v2>
  <customproperties>
    <property value="t_id" key="dualview/previewExpressions"/>
    <property value="0" key="embeddedWidgets/count"/>
    <property key="variableNames"/>
    <property key="variableValues"/>
  </customproperties>
  <blendMode>0</blendMode>
  <featureBlendMode>0</featureBlendMode>
  <layerOpacity>1</layerOpacity>
  <SingleCategoryDiagramRenderer diagramType="Histogram" attributeLegend="1">
    <DiagramCategory lineSizeType="MM" sizeType="MM" height="15" rotationOffset="270" scaleBasedVisibility="0" penColor="#000000" minScaleDenominator="0" opacity="1" scaleDependency="Area" lineSizeScale="3x:0,0,0,0,0,0" backgroundColor="#ffffff" barWidth="5" width="15" maxScaleDenominator="1e+08" penAlpha="255" labelPlacementMethod="XHeight" penWidth="0" backgroundAlpha="255" diagramOrientation="Up" sizeScale="3x:0,0,0,0,0,0" minimumSize="0" enabled="0">
      <fontProperties description=".SF NS Text,13,-1,5,50,0,0,0,0,0" style=""/>
      <attribute color="#000000" label="" field=""/>
    </DiagramCategory>
  </SingleCategoryDiagramRenderer>
  <DiagramLayerSettings placement="1" priority="0" dist="0" linePlacementFlags="18" showAll="1" zIndex="0" obstacle="0">
    <properties>
      <Option type="Map">
        <Option value="" type="QString" name="name"/>
        <Option name="properties"/>
        <Option value="collection" type="QString" name="type"/>
      </Option>
    </properties>
  </DiagramLayerSettings>
  <geometryOptions geometryPrecision="0" removeDuplicateNodes="0">
    <activeChecks/>
    <checkConfiguration/>
  </geometryOptions>
  <fieldConfiguration>
    <field name="fid">
      <editWidget type="TextEdit">
        <config>
          <Option/>
        </config>
      </editWidget>
    </field>
    <field name="t_id">
      <editWidget type="TextEdit">
        <config>
          <Option type="Map">
            <Option value="0" type="QString" name="IsMultiline"/>
            <Option value="0" type="QString" name="UseHtml"/>
          </Option>
        </config>
      </editWidget>
    </field>
    <field name="typ_kt">
      <editWidget type="TextEdit">
        <config>
          <Option/>
        </config>
      </editWidget>
    </field>
    <field name="typ_code_kommunal">
      <editWidget type="TextEdit">
        <config>
          <Option/>
        </config>
      </editWidget>
    </field>
    <field name="bfs_nr">
      <editWidget type="Range">
        <config>
          <Option/>
        </config>
      </editWidget>
    </field>
    <field name="artcode">
      <editWidget type="TextEdit">
        <config>
          <Option type="Map">
            <Option value="0" type="QString" name="IsMultiline"/>
            <Option value="0" type="QString" name="UseHtml"/>
          </Option>
        </config>
      </editWidget>
    </field>
  </fieldConfiguration>
  <aliases>
    <alias field="fid" index="0" name=""/>
    <alias field="t_id" index="1" name=""/>
    <alias field="typ_kt" index="2" name=""/>
    <alias field="typ_code_kommunal" index="3" name=""/>
    <alias field="bfs_nr" index="4" name=""/>
    <alias field="artcode" index="5" name=""/>
  </aliases>
  <excludeAttributesWMS/>
  <excludeAttributesWFS/>
  <defaults>
    <default field="fid" applyOnUpdate="0" expression=""/>
    <default field="t_id" applyOnUpdate="0" expression=""/>
    <default field="typ_kt" applyOnUpdate="0" expression=""/>
    <default field="typ_code_kommunal" applyOnUpdate="0" expression=""/>
    <default field="bfs_nr" applyOnUpdate="0" expression=""/>
    <default field="artcode" applyOnUpdate="0" expression=""/>
  </defaults>
  <constraints>
    <constraint exp_strength="0" field="fid" unique_strength="1" notnull_strength="1" constraints="3"/>
    <constraint exp_strength="0" field="t_id" unique_strength="1" notnull_strength="1" constraints="3"/>
    <constraint exp_strength="0" field="typ_kt" unique_strength="0" notnull_strength="0" constraints="0"/>
    <constraint exp_strength="0" field="typ_code_kommunal" unique_strength="0" notnull_strength="0" constraints="0"/>
    <constraint exp_strength="0" field="bfs_nr" unique_strength="0" notnull_strength="0" constraints="0"/>
    <constraint exp_strength="0" field="artcode" unique_strength="0" notnull_strength="0" constraints="0"/>
  </constraints>
  <constraintExpressions>
    <constraint exp="" desc="" field="fid"/>
    <constraint exp="" desc="" field="t_id"/>
    <constraint exp="" desc="" field="typ_kt"/>
    <constraint exp="" desc="" field="typ_code_kommunal"/>
    <constraint exp="" desc="" field="bfs_nr"/>
    <constraint exp="" desc="" field="artcode"/>
  </constraintExpressions>
  <expressionfields/>
  <attributeactions>
    <defaultAction value="{00000000-0000-0000-0000-000000000000}" key="Canvas"/>
    <actionsetting capture="0" id="{2bf1a58c-fc7a-4c2d-a877-3e9b24e46cb2}" notificationMessage="" isEnabledOnlyWhenEditable="0" type="5" icon="" action="[% &quot;dok_textimweb&quot; %]" shortTitle="" name="Dokument öffnen">
      <actionScope id="Canvas"/>
      <actionScope id="Field"/>
      <actionScope id="Feature"/>
    </actionsetting>
  </attributeactions>
  <attributetableconfig sortExpression="" actionWidgetStyle="dropDown" sortOrder="0">
    <columns>
      <column hidden="0" width="-1" type="field" name="t_id"/>
      <column hidden="1" width="-1" type="actions"/>
      <column hidden="0" width="-1" type="field" name="fid"/>
      <column hidden="0" width="-1" type="field" name="typ_kt"/>
      <column hidden="0" width="-1" type="field" name="typ_code_kommunal"/>
      <column hidden="0" width="-1" type="field" name="bfs_nr"/>
      <column hidden="0" width="-1" type="field" name="artcode"/>
    </columns>
  </attributetableconfig>
  <conditionalstyles>
    <rowstyles/>
    <fieldstyles/>
  </conditionalstyles>
  <editform tolerant="1">/vagrant/qgis/wms</editform>
  <editforminit/>
  <editforminitcodesource>0</editforminitcodesource>
  <editforminitfilepath>/vagrant/qgis/wms</editforminitfilepath>
  <editforminitcode><![CDATA[# -*- coding: utf-8 -*-
"""
QGIS forms can have a Python function that is called when the form is
opened.

Use this function to add extra logic to your forms.

Enter the name of the function in the "Python Init function"
field.
An example follows:
"""
from qgis.PyQt.QtWidgets import QWidget

def my_form_open(dialog, layer, feature):
	geom = feature.geometry()
	control = dialog.findChild(QWidget, "MyLineEdit")
]]></editforminitcode>
  <featformsuppress>0</featformsuppress>
  <editorlayout>generatedlayout</editorlayout>
  <editable>
    <field editable="1" name="artcode"/>
    <field editable="1" name="aussage"/>
    <field editable="1" name="bfs_nr"/>
    <field editable="1" name="fid"/>
    <field editable="1" name="subthema"/>
    <field editable="1" name="t_id"/>
    <field editable="1" name="thema"/>
    <field editable="1" name="typ_code_kommunal"/>
    <field editable="1" name="typ_kt"/>
  </editable>
  <labelOnTop>
    <field labelOnTop="0" name="artcode"/>
    <field labelOnTop="0" name="aussage"/>
    <field labelOnTop="0" name="bfs_nr"/>
    <field labelOnTop="0" name="fid"/>
    <field labelOnTop="0" name="subthema"/>
    <field labelOnTop="0" name="t_id"/>
    <field labelOnTop="0" name="thema"/>
    <field labelOnTop="0" name="typ_code_kommunal"/>
    <field labelOnTop="0" name="typ_kt"/>
  </labelOnTop>
  <widgets/>
  <previewExpression>t_id</previewExpression>
  <mapTip></mapTip>
  <layerGeometryType>2</layerGeometryType>
</qgis>
