var extraImagesCount = 0;

$(document).ready(function () {
	$("#buttonCancel").on("click", function () {
		window.location = moduleUrl;
	});
	
	$("#fileImage").change(function(){
		if (!checkFileSize(this)){
			return;
		}
		showImageThumbnail(this);				
	});
	
	// Products extra images
	$("input[name='extraImage'").each(function(index){
		extraImagesCount++;
		$(this).change(function(){			
			showExtraImageThumbnail(this, index);		
		});
	});
	
	$("a[name='linkRemoveExtraImage']").each(function(index){
		$(this).click(function(){
			removeExtraImage(index);
		})
	});
	
	$("a[name='linkRemoveDetail']").each(function(index){
		$(this).click(function(){
			removeDetailSectionByIndex(index);
		})
	});
	
	
});

function checkFileSize(fileInput){
	fileSize = fileInput.files[0].size;
	if (fileSize > MAX_FILE_SIZE){
		fileInput.setCustomValidity("You must choose an image less than " + MAX_FILE_SIZE + " bytes!");
		fileInput.reportValidity();
		return false;
	}else{
		fileInput.setCustomValidity("");
		return true;
	}				
}

function showImageThumbnail (fileInput) {
	var file = fileInput.files[0];
	var reader = new FileReader();
	reader.onload = function (e){
		$("#thumbnail").attr("src", e.target.result)
	}
	reader.readAsDataURL(file);
}	

function showExtraImageThumbnail (fileInput, index) {
	var file = fileInput.files[0];
	fileName = file.name;
	imageNameHiddenField = $("#imageName" + index);
	if(imageNameHiddenField.legnth){
		imageNameHiddenField(fileName);
	}
	var reader = new FileReader();
	reader.onload = function (e){
		$("#extraThumbnail" + index).attr("src", e.target.result)
	}
	reader.readAsDataURL(file);
	if(index >= extraImagesCount - 1){
		addNextExtraImageSection(index + 1);
	}
}	

function addNextExtraImageSection(index){
	htmlExtraImage = 
			`<div class="col border m-3 p-3" id="divExtraImage${index}">
				<div id="extraImageHeader${index}"><label class="font-weight-bold">Extra image #${index + 1}:</label></div>
				<div class="my-3">
					<img id="extraThumbnail${index}" alt="Extra image #${index + 1} preview" class="img-fluid"
						src="${defaultImageThumbnailSrc}" style="width: auto;"
					/>
				</div>
				<div>
					<input type="file" name="extraImage" 
						accept="image/png, image/jpeg"
						onchange="showExtraImageThumbnail(this, ${index})"
					/>
				</div>
			</div>`;
	htmlLinkRemove = `<a class="btn fas fa-times-circle fa-2x icon-green float-right" 
						href="javascript:removeExtraImage(${index -1})"
						title="Remove this image"></a>`;
			
	$("#divProductImages").append(htmlExtraImage);
	$("#extraImageHeader" + (index - 1)).append(htmlLinkRemove);
	extraImagesCount ++;
}

function removeExtraImage(index){
	$("#divExtraImage" + index).remove();
}

function removeDetailSectionByIndex(index){
	$("#divDetail" + index).remove();
}