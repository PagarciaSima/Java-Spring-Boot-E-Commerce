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

function showModalDialog(title, message, type){
	$('#modalTitle').text(title);
	iconElement = '<i class="ml-2 fa-solid fa-circle-exclamation modalIcon"></i>';
	$('#modalTitle').append(iconElement);
	if(type == 'error'){
		$('#modalHeader').removeClass("alert-warning");
		$('#modalHeader').addClass("alert-danger")
	}else{
		$('#modalHeader').removeClass("alert-danger");
		$('#modalHeader').addClass("alert-warning")
	}
	$('#modalBody').text(message);
	$("#modalDialog").modal();
}

