window.ListArea = {
  props: {
    rows: {
      type: Array,
      required: true
    }
  },
  setup(props, {emit}) {
    return {
    };
  },
  template: `
      <div class="list">
        <div v-if="rows && rows.length > 0">
          <div v-for="(row, index) in rows" class="item" 
              :key="index" :data-row-id="row.id || row.name">
            <div :class="row.confidence >= 0.9 ? 'green' : row.confidence >= 0.7 ? 'warning' : 'error'">
              <input type="text" v-model="row.text">
            </div>
          </div>
        </div>
        <div v-else class="noData">
          <span>식별된 데이터가 없습니다</span>
        </div>
      </div>
    `
}