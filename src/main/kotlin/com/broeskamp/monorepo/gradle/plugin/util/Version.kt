package com.broeskamp.monorepo.gradle.plugin.util

data class Version private constructor(
  /**
   * Retrieves the MAJOR part of the version object.
   * @return the number for the major version
   */
  val major: Int,
  /**
   * Retrieves the MINOR part of the version object.
   * @return the number for the minor version
   */
  val minor: Int,
  /**
   * Retrieves the PATCH part of the version object.
   * @return the number for the patch version
   */
  val patch: Int,
  val qualifiers: List<String>,
  private val stringRepresentation: String
) {

  override fun toString(): String = stringRepresentation

  companion object {
    private val globalVersionPattern = Regex(
      "^([0-9]+)(?:\\.([0-9]+))?(?:\\.([0-9]+))?([\\-\\+][a-zA-Z0-9][a-zA-Z0-9\\-_\\.\\+]*)?$"
    )

    /**
     * Creates a [Version] object by parsing the given string.
     * @param versionAsString the string to parse
     * @return a Version object built from the information of the given representation
     * @throws IllegalStateException if the given string doesn't match the version
     */
    fun parse(versionAsString: String): Version {
      globalVersionPattern.matchEntire(versionAsString)?.run {
        val major = groups[1]!!.value.toInt()
        val minor = groups[2]?.value?.toInt() ?: 0
        val patch = groups[3]?.value?.toInt() ?: 0

        val qualifiers: List<String> = groups[4]?.run qual@{
          return@qual value.replaceFirst("-", "").split("-")
        } ?: emptyList()

        val stringRepresentation = StringBuilder()
        stringRepresentation.append(String.format("%d.%d.%d", major, minor, patch))
        qualifiers.forEach {
          if (!it.startsWith("-") && !it.startsWith("+")) {
            stringRepresentation.append('-')
          }
          stringRepresentation.append(it)
        }
        return Version(major, minor, patch, qualifiers, stringRepresentation.toString())
      }
      throw IllegalStateException("cannot parse $versionAsString as a semver compatible version")
    }
  }
}
