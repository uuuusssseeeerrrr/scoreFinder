window.CombatArea = {
  props: {
    isCombatReady: {
      type: Boolean,
      required: true
    },
    rows: {
      type: Array,
      required: true
    }
  },
  emits: ['closeCombatClick'],
  setup(props, {emit}) {
    const numberFormatter = new Intl.NumberFormat('ko-KR');

    const closeClick = () => {
      emit('closeCombatClick');
    }

    return {
      numberFormatter,
      closeClick,
    };
  },
  template: `
      <div
        :class="{ open: isCombatReady }"
        class="detailsPanel"
        role="dialog"
        aria-modal="true">
          <div class="detailsHeader">
            <div class="detailsTitle">점수 조회 결과</div>
            <div class="closeX detailsClose" @click="closeClick">×</div>
          </div>
        
          <div class="skillHeader">
            <div class="cell name center">이름</div>
            <div class="cell center hit">서버명</div>
            <div class="cell center crit">전투력</div>
            <div class="cell center parry">점수(아툴)</div>
          </div>
  
          <div class="skills">
            <div 
              v-for="row in rows" 
              :key="row.text"
              class="skillRow">
              
              <div class="cell name">{{ row.text }}</div>
              <div class="cell center">{{ row.server }}</div>
              <div class="cell center">{{ row.combatPower || "정보 없음" }}</div>
              <div class="cell center">{{ row.combatScore ? numberFormatter.format(row.combatScore) : "정보 없음" }}</div>
            </div>
          </div>
        </div>
      </div>
    `
}