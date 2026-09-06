export default function Logo({ className = '' }) {
  // TODO: public 폴더에 원하는 아이콘 파일을 넣고 아래 파일명을 수정하세요. (예: '/my-icon.svg')
  const iconUrl = '/logo.png' 

  return (
    <img 
      src={iconUrl} 
      alt="골라골라 로고" 
      // 크기는 컴포넌트를 호출하는 쪽에서 className으로 완전히 제어하도록 변경
      className={`w-auto object-contain inline-block align-middle ${className}`} 
    />
  )
}
