<div align="center">
<img src="https://user-images.githubusercontent.com/67344817/202866168-a0b8787f-84c8-49c9-b218-b4a773bba231.png" width="150"></img>

# ConfigurationLib
Advanced configuration library with mapping for Bukkit!<br>

![Badge](https://img.shields.io/jitpack/version/com.github.ZorTik/ConfigurationLib?style=for-the-badge) ![Badge](https://img.shields.io/github/license/ZorTik/ConfigurationLib?style=for-the-badge)
</div>

```java
@AllArgsContructor
class Item {
  private String name;
  private String description;
}

SectionNode<?> node = ...;
node.set("items.item1", new User("Item 1", "The first item"));
Item item = node.getSection("items.item1").map(Item.class);
```
becomes
```yaml
items:
  item1:
    name: Item 1
    description: The first item
```
More on wiki!

## About
This library is designed to help you build Bukkit configurations quick and easy. It has bunch of good features that you don't want to miss. You can see some of them through official wiki on this repository.

## Wiki
For fast intro, I recommend official wiki section on this repository. <a href="https://github.com/ZorTik/ConfigurationLib/wiki">Click to open Wiki</a> <br>
Or you can see my <a href="https://github.com/ZorTik/ConfigurationLib/tree/master/examples">examples.</a>

## Implementation
To add this library to your project, you can use one of these methods. I recommend shading the library using shadowJar to avoid version concurrency issues.

<a href="https://github.com/ZorTik/ConfigurationLib/wiki/Implementation">Implementation on Wiki</a>


## Attributions
<a href="https://www.flaticon.com/free-icons/document" title="document icons">Document icons created by vectorsmarket15 - Flaticon</a>
