Librarie générique d'import de fichiers en java
======

##Description

Cette librairie permet de transformer automatiquement des fichiers csv, xls et xml en objets java. La configuration de cet import se fait par des fichiers xml.

##Utilisation

Le code ci-dessous est le code nécessaire et suffisant pour utiliser la librairie.
```
ImportFactoryBean factory = new ImportFactoryBean();
factory.setMappingResources("mapping.xml");
factory.buildImportFactory();

Importer importer = factory.buildImporter("myMapping");
List<MyObject> objects = importer.importFrom(MyObject.class, "file.csv");
```

###ImportFactoryBean

La classe de base pour utiliser la librairie est la classe ``` ImportFactoryBean ```. 
Elle peut s'instancier de manière manuelle ou grâce à Spring.

####Instanciation manuelle

```
ImportFactoryBean factory = new ImportFactoryBean();
factory.setMappingResources("mapping.xml");
```

####Instanciation via Spring
```
<bean id="importFactory" class="com.equinox.imports.ImportFactoryBean">
	<property name="mappingResources">
		<list>
			<value>mapping.xml</value>
		</list>
	</property>
</bean>
```

Une fois la factory instanciée, elle doit être buildée pour être utilisée.
```
factory.buildImportFactory();
```

###Importer
```Importer``` est une interface fonctionnelle qui va permettre d'effectuer réellement l'import des objets depuis le (ou les) fichier(s). Une implémentation de cette interface est retournée par la méthode ```ImportFactoryBean#buildImporter(String)``` qui prend en paramètre l'identifiant défini dans le fichier xml de mapping.

La méthode ```Importer#importFrom(Class<T>, String...)``` prend en paramètre la classe à retourner ainsi que les fichiers dans lesquels chercher et va retourner la liste d'objets extraits du fichier.

##Configuration

Soit une classe
``` 
package foo;

public class MyObject {
  private String stringField;
  private Integer integerField;
  
  public MyObject() { }
  
  public void setStringField(String stringField) { this.stringField = stringField; }
  public String getStringField() { return stringField; }
  
  public void setIntegerField(Integer integerField) { this.integerField = integerField; }
  public Integer getIntegerField() { return integerField; }
  
}
```
Le fichier de configuration minimal pour l'importer depuis un fichier csv est le suivant :
```
<?xml version="1.0" encoding="UTF-8"?>

<import-mapping id="myMapping">
	<class name="foo.MyObject">
		<file id="main" type="csv" delimitor=";" />
	  
		<property file-ref="main" name="stringField" column="0" type="java.lang.String" />
		<property file-ref="main" name="integerField" column="1" type="java.lang.Integer" />
	</class>
</import-mapping>
```

###Fichiers
Les fichiers sont définis par l'élément ```File```. Il est obligatoire d'avoir au moins un fichier défini par classe.
Les attributs possibles sont :
- `id` : obligatoire. Il donne un nom unique au fichier pour le référencer dans le reste de la configuration.
- `type` : obligatoire. Les types disponibles sont csv, xls ou xml

Pour les fichiers csv, il est aussi possible d'utiliser :
- `delimitor` : obligatoire. Il indique le caractère servant à délimiter les colonnes.
- `file-number` : pas obligatoire. Il indique le numéro qui doit être présent en préfixe du nom du fichier.
- `skip-lines` : pas obligatoire. Il indique le nombre de lignes à sauter avant de lire le contenu du fichier.
- `start-tag` : pas obligatoire. Il indique la balise qui va marquer le début du contenu à lire dans le fichier.
- `end-tag` : pas obligatoire. Il indique la balise qui va marquer le fin du contenu à lire dans le fichier.
- `labels` : pas obligatoire. Il indique s'il faut se baser sur le numéro des colonnes (0-based) ou sur leur nom. Si `labels` est vrai, la première ligne lue va devoir contenir le nom des colonnes.

Pour les fichiers xls, il est aussi possible d'utiliser :
- `worksheet` : obligatoire. Il indique le nom de la feuille de calcul dans laquelle lire le contenu.
- `labels`: cf-ci-dessus.
- `skip-lines` : cf-ci-dessus.

Pour les fichiers xml, il est aussi possible d'utiliser :
- `base-element` : obligatoire. Il indique le nom de l'élément de base dans lequel lire le contenu.

####Filtres
Si l'on ne veut pas récupérer toutes les lignes, il est possible de filtrer le fichier sur une colonne particulière en utiliser un `ImportFileFilter`. Il s'agit d'une interface fonctionnelle qui possède une méthode `filter(String)` qui prend en paramètre la valeur (sous forme de String) de la colonne qui va servir à filtrer.
On peut ainsi filtrer un fichier particulier sur une colonne qui ne servira pas à construire les objets finaux.

Par exemple, si on veut garder seulement les lignes dont la colonne 4 est égale à `foo`, on peut écrire le filtre suivant.

```
package foo.filter;

import com.equinox.imports.file.ImportFileFilter;

public class FooFilter implements ImportFileFilter {

  @Override
  public boolean filter(String value) {
    return "foo".equals(value);
  }
}
```

Dans le fichier de configuration, il va être déclaré comme ceci :
```
<file id="main" type="csv" delimitor=";">
 	<filter column="4" class="foo.filter.FooFilter" />
</file>
```

####Clés
Si l'on veut utiliser plusieurs fichiers joins ensemble pour importer les objets, il est possible de le faire à l'aide des clés. 
En pratique, un fichier peut avoir plusieurs clés ainsi que des sous-fichiers. Chaque sous fichier doit alors définir au moins une clé qui doit être associée à l'une des clés du fichier parent. Lors de la lecture des fichiers, toutes les lignes dont les clés seront égales seront associées (comme une jointure en SQL).

La configuration d'un fichier avec un sous fichier se fait comme suit :
```
<file id="main" type="csv" file-number="1" delimitor=";">
  <key id="keyInFileOne" type="java.lang.Long" column="0" />
  
  <file id="second" type="csv" file-number="2" delimitor=";">
    <key id="keyInFileTwo' type="java.lang.Long" column="0" key-ref="keyInFileOne" />
  </file>
</file>
```

Si une clé est composée de plusieurs colonnes, il est possible de définir une clé par colonne dans le fichier parent et dans le sous fichier.

###Propriétés
Une propriété fait le lien entre les colonnes dans le(s) fichier(s) et les propriétés de l'objet à créer. La propriété de l'objet Java doit exister (pas seulement un setteur) et être accessible (publique ou disposer d'un setteur).

La propriété est définie par l'élément `property`. 
Les attributs possibles sont :
- `file-ref` : obligatoire. Indique dans quel fichier la propriété doit être lue.
- `name` : obligatoire. Indique le nom de la propriété dans l'objet Java.
- `column` : obligatoire. Indique le nom ou l'index (0-based) de la colonne dans le fichier. Dans le cas d'un fichier xml, la colonne est l'ensemble de la hiérarchie des éléments avec l'attribut éventuel entre crochet (`Root/Foo[bar]` dans le cas `<Root><Foo bar="value" /></Root>` ou `Root/Foo` dans le cas `<Root><Foo>value</Foo></Root>` avec `Root` égal à l'attribut `base-element` du fichier).
- `type` : pas obligatoire. Indique le type de la propriété de l'objet Java.
- `length` : pas obligatoire. Indique la taille maximale de la propriété de l'objet Java (sur laquelle la méthode `Object#toString()` a été appelée).
- `not-null` : pas obligatoire. Indique si la propriété de l'objet Java peut être nulle ou non.
- `multiple` : pas obligatoire. Indique si la propriété de l'objet Java est une liste de `type`.
- `default-value` : pas obligatoire. Indique la valeur par défaut de la colonne (valeur prise si la colonne est vide). Si une valeur par défaut est définie, l'attribut `column` n'est plus obligatoire.

####Propriétés composites
Quand la propriété de l'objet Java est construite à partir de plusieurs colonnes du fichier, il est possible d'utiliser une propriété composite associée à un `CompositeImportPropertyComposer`. Cette interface fonctionnelle possède une méthode `compose(Map<String, String>)` qui retourne un `Object` et prend en paramètre l'ensemble des valeurs des colonnes de la ligne, indexées par le nom de la sous propriété. 

Par exemple, si la propriété `bar` est la concaténation des colonnes `bar1` et `bar2`, on peut écrire le compositeur suivant :

```
package com.foo;

import com.equinox.imports.CompositeImportPropertyComposer;

public class BarPropertyComposer implements CompositeImportPropertyComposer {

  @Override
  public String compose(Map<String, String> values) throws ImportPropertyException {
    return values.get("bar1") + values.get("bar2");
  }

}
```

La configuration d'une propriété composite se fait comme suit :
```
<composite-property name="bar" type="java.lang.String" compose-class="com.foo.BarPropertyComposer">
	<component-property file-ref="main" name="bar1" column="4" />
	<component-property file-ref="main" name="bar2" column="7" />
</composite-property>
```

L'élément `composite-property` peut avoir les attributs suivants :
- `name` : cf. propriété.
- `type` : cf. propriété.
- `not-null`: cf. propriété.
- `multiple`: cf. propriété.
- `compose-class` : obligatoire. Indique la classe qu va servir à composer la propriété.

####Propriétés sous-classes
Quand la propriété de l'objet Java est une classe dont les propriétés sont elles-mêmes lues dans le fichier, il est possible d'utilisé une propriété sous-classe.

Soit les classes suivantes :
``` 
package foo;

public class Bar {
  private MyObject object;
  
  public Bar() { }
  
  public void setObject(MyObject object) { this.object = object; }
  public MyObject getObject() { return object; }
  
}
```

``` 
package foo;

public class MyObject {
  private String stringField;

  public MyObject() { }
  
  public void setStringField(String stringField) { this.stringField = stringField; }
  public String getStringField() { return stringField; }

}
```

La configuration de la lecture de la propriété `object` de `Bar` se fait comme ceci :
```
<sub-class-property type="com.foo.MyObject" name="object">
	<property file-ref="main" name="stringField" column="4" />
</sub-class-property>
```

L'élément `sub-class-property` peut avoir les attributs suivants :
- `name` : cf. propriété.
- `type` : cf. propriété.
- `not-null`: cf. propriété.
- `multiple`: cf. propriété.

###Transformateurs
Quand les propriétés ou les clés ne sont pas des types de base ou doivent être transformées avant d'être settées, il est possibile d'utiliser un transformateur. L'interface `ImportPropertyTransformer` possède une méthode `transformProperty(value)` qui retourne un `Object` et qui prend en paramètre la valeur de la colonne à transformer.

Si l'on veut préfixer une propriété avec une chaine de caractère, il est possible d'écrire le filtre suivant :
```
package com.foo.transform;

import com.equinox.imports.transformer.ImportPropertyTransformer;

public class FooPrefixPropertyTransformer implements ImportPropertyTransformer {
	
	@Override 
	public String transformProperty(String value) {
		return "Foo : " + value;
	}
}
```

La configuration de la propriété sera alors :
```
<property file-ref="main" name="foo" column="6" type="java.lang.String" transform-class="com.foo.FooPrefixPropertyTransformer" />
```

Cet attribut peut s'appliquer aux propriétés (même composite ou sous-classes) et aux clés.

Les types de base gérés par la librairie sont :
- `String` : la colonne est retournée telle quelle.
- `Long`: la colonne est parsée en `Long` par la méthode `Long.valueOf(String)`.
- `Integer` : la colonne est parsée en `Integer` par la méthode `Integer.valueOf(String)`.
- `Double` : la colonne est parsée en `Double` par la méthode `Double.valueOf(String)`.
- `Boolean` : la colonne est `true` si égale à 1 ou `true`, `false` sinon.
- `Date`: la colonne est parsée en `Date` en suivant le format `yyyy-MM-dd - HH:mm:ss`.

Pour tous les autres types ou autres format, il faut faire des transformateurs personnalisés.

###Générateurs
Quand une propriété doit être générée, quelle que soit la valeur des colonnes, il faut passer par un générateur. L'interface `ImportPropertyGenerator` possède une méthode `generate` qui retourne un `Object`. Quand un générateur est appliqué, l'attribut `column` n'est plus obligatoire.

Si l'on veut générer une propriété en suivant une séquence, il est possible d'écrire le générateur suivant :
```
package com.foo.generator;

import com.equinox.imports.ImportPropertyGenerator;

public class FooSequenceGenerator implements ImportPropertyGenerator {

	private Integer ordreCourant = 0;
	
	@Override 
	public Integer generate() {
		return ordreCourant++;
	}
}
```

La configuration de la propriété sera alors :
```
<property file-ref="main" name="foo" type="java.lang.Integer" generator-class="com.foo.FooSequenceGenerator" />
```

Cet attribut peut s'appliquer aux propriétés (même composite ou sous-classes) et aux clés.

###Post-processeurs
Certaines vérifications et certains traitements ne peuvent être appliqués à l'objet qu'une fois que toutes les propriétés ont été settées. Pour ce genre de cas, il faut utiliser une `post-process-class` qui va traiter l'objet une fois finalisé.
L'interface `ImportClassPostProcessor` possède une méthode `postProcess(Object)` qui prend en paramètre l'objet final.

Soit la classe 
``` 
package foo;

public class MyObject {
  private Integer foo;
  private Integer bar;
  
  public MyObject() { }
  
  // Getters et Setters ...
  
}
```

Si l'on veut que l'objet soit créé seulement si `foo` est supérieur à `bar`, il est possible d'écrire le post-processeur suivant :
```
package com.foo.postProcess;

import com.equinox.imports.ImportClassPostProcessor;

public class FooBarPostProcessor implements ImportClassPostProcessor {

	@Override
	public void postProcess(Object object) throw ImportPropertyException {
		MyObject obj = (MyObject) object;
		if (obj.getFoo() < obj.getBar()) {
			throw new ImportPropertyException("Foo must be greater than bar");
		}
	}

}
```

La configuration de la classe se fera alors comme ceci :
```
<class name="com.foo.MyObject" post-process-class="com.foo.postProcess.FooBarPostProcessor">
	<!-- Déclaration des propriétés ... -->
</class>
```

###Exceptions
Les méthodes des interface `CompositeImportPropertyComposer`, `ImportClassPostProcessor` et `ImportPropertyTransformer` peuvent renvoyer des exceptions du type `ImportPropertyException`. Ces exceptions sont lancées lorsqu'il est impossible de créer l'objet à cause d'une propriété particulière.
Des exceptions héritant de `ImportPropertyException` ont été définies et peuvent être utilisées dans les compositeurs, post-processeurs et transformeurs personnalisés :
- `InvalidFormatPropertyImportException` : peut être lancée quand la colonne n'est pas au bon format. Le constructeur de cette exception prend en argument le nom de la propriété, le format attendu, le format réel et une exception éventuelle.
- `NullPropertyImportException` : peut être lancée quand la colonne est nulle et ne devrait pas. Le constructeur de cette exception prend en argument seulement le nom de la propriété.
- `TooLongPropertyImportException` : peut être lancée quand la valeur de la colonne est trop long. Le constructeur de cette exception prend en argument le nom de la propriété, la taille maximale et la taille réelle.

##Licence

Cette librairie est développée sous licence MIT.

Copyright © 2014, Renaud Humbert-Labeaumaz

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

The Software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement. In no event shall the authors or copyright holders X be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the software or the use or other dealings in the Software.

Except as contained in this notice, the name of Renaud Humbert-Labeaumaz shall not be used in advertising or otherwise to promote the sale, use or other dealings in this Software without prior written authorization from Renaud Humbert-Labeaumaz.
