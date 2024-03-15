$(document).ready(function(){
	$("#logoutLink").on("click", function(e){
		e.preventDefault();
		document.logoutForm.submit();
	});
	customizeNavbarDropDownMenu();
});

function customizeNavbarDropDownMenu(){
	$(".navbar .dropdown").hover(
		function(){
			$(this).find('.dropdown-menu').first().stop(true, true).delay(250).slideDown();
		},
		function(){
			$(this).find('.dropdown-menu').first().stop(true, true).delay(100).slideUp();
		}
	);
}

