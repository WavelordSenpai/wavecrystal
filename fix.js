// Make onload safe - one crash can't kill everything
window.addEventListener('load', ()=>{
  setTimeout(()=>{
    try{ if(typeof init3D==='function') init3D(); }catch(e){ console.log('3D failed, using fallback'); }
    try{ if(typeof showRitual==='function') showRitual('icosa'); }catch(e){}
    try{ if(typeof updateTimerUI==='function') updateTimerUI(); }catch(e){}
    try{ if(typeof loadDailyWord==='function') loadDailyWord(false); }catch(e){}
    try{ if(typeof renderSaved==='function') renderSaved(); }catch(e){}
  },100);
});
// FIX: unblock touches + crystal fallback + dictionary fallback
document.addEventListener('DOMContentLoaded', ()=>{
  // 1. Unblock touches - any fullscreen overlay covering buttons gets disabled
  document.querySelectorAll('div,section').forEach(el=>{
    let s=getComputedStyle(el);
    if((s.position==='fixed'||s.position==='absolute') && parseInt(s.zIndex)>50){
      if(!el.querySelector('button,input,textarea')) el.style.pointerEvents='none';
    }
  });
  // 2. Make sure all buttons/inputs are clickable
  document.querySelectorAll('button,input,textarea,canvas').forEach(el=>{
    el.style.pointerEvents='auto';
    el.style.touchAction='manipulation';
  });
  document.body.style.pointerEvents='auto';

  // 3. Crystal fallback - if the crystal box is still empty/black after 2 sec, draw a CSS crystal
  setTimeout(()=>{
    let box=document.querySelector('#crystalBox, #crystal, .crystal, canvas');
    let canvas=document.querySelector('canvas');
    if(canvas){
      let ctx=canvas.getContext('2d');
      // check if canvas is blank
      let data=ctx.getImageData(0,0,10,10).data;
      let blank=data.every(v=>v===0);
      if(blank){
        let div=document.createElement('div');
        div.innerHTML='<div style="font-size:90px;text-align:center;animation:spin 4s linear infinite">⬢</div><style>@keyframes spin{to{transform:rotate(360deg)}}</style>';
        canvas.parentNode.insertBefore(div, canvas);
        canvas.style.display='none';
      }
    }
  },2000);

  // 4. Dictionary fallback - if your dictionary button exists but does nothing, fix it
  let dictBtn=document.querySelector('#dictBtn, [id*=dict i], button');
  // only attach if there's a dict input
  let dictInput=document.querySelector('#dictInput, input[placeholder*=word i], input[placeholder*=dictionary i]');
  let dictOut=document.querySelector('#dictResult, #dictOutput, #definition');
  if(dictInput && dictBtn && dictOut && !dictBtn.dataset.fixed){
    dictBtn.dataset.fixed='1';
    dictBtn.addEventListener('click', async ()=>{
      let w=dictInput.value.trim();
      if(!w){ dictOut.textContent='Type a word first.'; return; }
      dictOut.textContent='Looking up "'+w+'" ...';
      try{
        let r=await fetch('https://api.dictionaryapi.dev/api/v2/entries/en/'+encodeURIComponent(w));
        let j=await r.json();
        let m=j[0].meanings[0];
        dictOut.textContent=j[0].word+' ('+m.partOfSpeech+')\n\n'+m.definitions[0].definition;
      }catch(e){ dictOut.textContent='Could not find "'+w+'". Check spelling / internet.'; }
    });
  }
});
