/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Sebastian Sdorra
 */
@SuppressWarnings("unchecked")
public final class ExtensionCollector
{

  private static final Logger LOG = LoggerFactory.getLogger(ExtensionCollector.class);

  private final Set<String> pluginIndex;

  public ExtensionCollector(ClassLoader moduleClassLoader, Set<ScmModule> modules, Set<InstalledPlugin> installedPlugins) {
    this.pluginIndex = createPluginIndex(installedPlugins);

    for (ScmModule module : modules) {
      collectRootElements(module);
    }
    for (ScmModule plugin : PluginsInternal.unwrap(installedPlugins)) {
      collectRootElements(plugin);
    }

    for (ScmModule module : modules) {
      collectExtensions(moduleClassLoader, module);
    }

    for (InstalledPlugin plugin : installedPlugins) {
      collectExtensions(plugin.getClassLoader(), plugin.getDescriptor());
    }
  }

  private Set<String> createPluginIndex(Set<InstalledPlugin> installedPlugins) {
    return installedPlugins.stream()
      .map(p -> p.getDescriptor().getInformation().getName())
      .collect(Collectors.toSet());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param epe
   *
   * @return
   */
  public Collection<Class> byExtensionPoint(ExtensionPointElement epe)
  {
    Collection<Class> collection = extensions.get(epe);

    if (collection == null)
    {
      collection = Collections.EMPTY_SET;
    }

    return collection;
  }

  /**
   * Method description
   *
   *
   * @param clazz
   *
   * @return
   */
  public Collection<Class> byExtensionPoint(Class clazz)
  {
    Collection<Class> exts;
    ExtensionPointElement epe = extensionPointIndex.get(clazz);

    if (epe != null)
    {
      exts = byExtensionPoint(epe);
    }
    else
    {
      exts = Collections.EMPTY_SET;
    }

    return exts;
  }

  /**
   * Method description
   *
   *
   * @param epe
   *
   * @return
   */
  public Class oneByExtensionPoint(ExtensionPointElement epe)
  {
    return Iterables.getFirst(byExtensionPoint(epe), null);
  }

  /**
   * Method description
   *
   *
   * @param clazz
   *
   * @return
   */
  public Class oneByExtensionPoint(Class clazz)
  {
    return Iterables.getFirst(byExtensionPoint(clazz), null);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<ExtensionPointElement> getExtensionPointElements()
  {
    return extensionPointIndex.values();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Multimap<ExtensionPointElement, Class> getExtensions()
  {
    return extensions;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<Class> getLooseExtensions()
  {
    return looseExtensions;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<Class> getRestProviders()
  {
    return restProviders;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<Class> getRestResources()
  {
    return restResources;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<WebElementDescriptor> getWebElements()
  {
    return webElements;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param extension
   */
  private void appendExtension(Class extension)
  {
    boolean found = false;

    for (Entry<Class, ExtensionPointElement> e : extensionPointIndex.entrySet())
    {
      if (e.getKey().isAssignableFrom(extension))
      {
        extensions.put(e.getValue(), extension);
        found = true;

        break;
      }
    }

    if (!found)
    {
      looseExtensions.add(extension);
    }
  }

  private void collectExtensions(ClassLoader defaultClassLoader, ScmModule module) {
    for (ExtensionElement extension : module.getExtensions()) {
      if (isRequirementFulfilled(extension)) {
        Class<?> extensionClass = loadExtension(defaultClassLoader, extension);
        appendExtension(extensionClass);
      }
    }
  }

  private Class<?> loadExtension(ClassLoader classLoader, ExtensionElement extension) {
    try {
      return classLoader.loadClass(extension.getClazz());
    } catch (ClassNotFoundException ex) {
      throw new PluginLoadException("failed to load clazz", ex);
    }
  }

  private boolean isRequirementFulfilled(ExtensionElement extension) {
    if (extension.getRequires() != null) {
      for (String requiredPlugin : extension.getRequires()) {
        if (!pluginIndex.contains(requiredPlugin)) {
          LOG.debug("skip loading of extension {}, because the required plugin {} is not installed", extension.getClazz(), requiredPlugin);
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Method description
   *
   *
   * @param module
   */
  private void collectRootElements(ScmModule module)
  {
    for (ExtensionPointElement epe : module.getExtensionPoints())
    {
      extensionPointIndex.put(epe.getClazz(), epe);
    }

    restProviders.addAll(Lists.newArrayList(module.getRestProviders()));
    restResources.addAll(Lists.newArrayList(module.getRestResources()));
    Iterables.addAll(webElements, module.getWebElements());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Set<WebElementDescriptor> webElements = Sets.newHashSet();

  /** Field description */
  private final Set<Class> restResources = Sets.newHashSet();

  /** Field description */
  private final Set<Class> restProviders = Sets.newHashSet();

  /** Field description */
  private final Set<Class> looseExtensions = Sets.newHashSet();

  /** Field description */
  private final Multimap<ExtensionPointElement, Class> extensions =
    HashMultimap.create();

  /** Field description */
  private final Map<Class, ExtensionPointElement> extensionPointIndex =
    Maps.newHashMap();
}
