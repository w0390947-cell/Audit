<template>
  <AuditLibraryFolder resource-scope="task" :allow-upload="false" />
</template>

<script>
import AuditLibraryFolder from './folder.vue'

export default {
  name: 'AuditLibraryTask',
  components: { AuditLibraryFolder }
}
</script>
