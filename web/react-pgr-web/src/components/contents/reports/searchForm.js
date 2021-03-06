import React, {Component} from 'react';
import {connect} from 'react-redux';
import {Grid, Row, Col, Table, DropdownButton} from 'react-bootstrap';
import {Card, CardHeader, CardText} from 'material-ui/Card';
import TextField from 'material-ui/TextField';
import {brown500, red500,white,orange800} from 'material-ui/styles/colors';
import DatePicker from 'material-ui/DatePicker';
import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem';
import RaisedButton from 'material-ui/RaisedButton';
import Api from '../../../api/api';
import ShowField from './showField';
import jp from "jsonpath";
import _ from 'lodash';


const styles = {
  errorStyle: {
    color: red500
  },
  underlineStyle: {
    borderColor: brown500
  },
  underlineFocusStyle: {
    borderColor: brown500
  },
  floatingLabelStyle: {
    color: brown500
  },
  floatingLabelFocusStyle: {
    color: brown500
  },
  customWidth: {
    width:100
  }
};

class ShowForm extends Component {
  constructor(props) {
       super(props);
       this.state = {
         searchBtnText : 'Search'
       }
       this.search=this.search.bind(this);
   }

  handleChange=(e, property, isRequired, pattern)=>
  {

    // console.log(e);
    let {metaData,setMetaData}=this.props;
    this.props.handleChange(e, property, isRequired, pattern);

    // console.log(metaData);
    if(metaData.hasOwnProperty("reportDetails") && metaData.reportDetails.searchParams.length > 0)
    {
      for (var i = 0; i < metaData.reportDetails.searchParams.length; i++) {
        if (metaData.reportDetails.searchParams[i].type=="url" && (typeof(metaData.reportDetails.searchParams[i].defaultValue)!="object" || metaData.reportDetails.searchParams[i].hasOwnProperty("pattern"))) {
            if (!metaData.reportDetails.searchParams[i].hasOwnProperty("pattern")) {
              metaData.reportDetails.searchParams[i]["pattern"]=metaData.reportDetails.searchParams[i].defaultValue;
            }
            // console.log(metaData.reportDetails.searchParams[i].type);
            if(metaData.reportDetails.searchParams[i].pattern.search(property)>-1)
            {
              let splitArray=metaData.reportDetails.searchParams[i].pattern.split("?");
              let context="";
              let id={};
              id[splitArray[1].split("&")[1].split("=")[0]]=e.target.value;
              for (var j = 0; j < splitArray[0].split("/").length; j++) {
                context+=splitArray[0].split("/")[j]+"/";
              }

              var response=Api.commonApiPost(context,id).then(function(response)
              {
                  let keys=jp.query(response,splitArray[1].split("|")[1]);
                  let values=jp.query(response,splitArray[1].split("|")[2]);
                  let defaultValue={};
                  for (var k = 0; k < keys.length; k++) {
                    defaultValue[keys[k]]=values[k];
                  }
                  for (var l = 0; l < metaData.reportDetails.searchParams.length; l++) {
                    if (metaData.reportDetails.searchParams[l].type=="url" && metaData.reportDetails.searchParams[l].pattern.search(property)>-1) {
                        metaData.reportDetails.searchParams[l].defaultValue=defaultValue;
                    }
                  }
                  // console.log(defaultValue);
                  // console.log(i);
                  // console.log(metaData);
                  setMetaData(metaData);
              },function(err) {
                  alert("Somthing went wrong while loading depedant dropdown");
              });
            }
        }
      }
    }

  }


  handleFormFields = () => {
    let {currentThis}=this;
    let {metaData,searchForm}=this.props;
    // console.log(metaData);
    if(!_.isEmpty(metaData) && metaData.reportDetails.searchParams.length > 0)
    {
      return metaData.reportDetails.searchParams.map((item,index) =>
      {
        item["value"]=_.isEmpty(searchForm)?"":searchForm[item.name];
        return (
          <ShowField key={index} obj={item} handler={this.handleChange}/>
        );
      })
    }
  }

  componentDidMount()
  {
    let {initForm,metaData,setForm} = this.props;
    let {searchParams}=!_.isEmpty(metaData)?metaData.reportDetails:{searchParams:[]};
    let required=[];
    for (var i = 0; i < searchParams.length; i++) {
      if (searchParams.isMandatory || searchParams.hasOwnProperty("isMandatory")?false:true) {
        required.push(searchParams.name)
      }
    }
    initForm();
    setForm(required);
  }



  search(e)
  {
    e.preventDefault();

      let {showTable,changeButtonText,setReportResult,searchForm,metaData,setFlag,setSearchParams}=this.props;
      let searchParams=[]


      for (var variable in searchForm) {
        // console.log(variable);

        searchParams.push({
          name:variable,
          // value:typeof(searchForm[variable])=="object"?new Date(searchForm[variable]).getTime():searchForm[variable]
          input:typeof(searchForm[variable])=="object"?variable=="fromDate"?(new Date(searchForm[variable]).getFullYear() + "-" + (new Date(searchForm[variable]).getMonth()>9?(new Date(searchForm[variable]).getMonth()+1):("0"+(new Date(searchForm[variable]).getMonth()+1))) + "-" +(new Date(searchForm[variable]).getDate()>9?new Date(searchForm[variable]).getDate():"0"+new Date(searchForm[variable]).getDate())+" "+"00:00:00"):(new Date(searchForm[variable]).getFullYear() + "-" + (new Date(searchForm[variable]).getMonth()>9?(new Date(searchForm[variable]).getMonth()+1):("0"+(new Date(searchForm[variable]).getMonth()+1))) + "-" +(new Date(searchForm[variable]).getDate()>9?new Date(searchForm[variable]).getDate():"0"+new Date(searchForm[variable]).getDate())+" "+"23:59:59"):searchForm[variable]

        })
      }

      // searchParams.push(
      //   {
      //       "name": "complainttype",
      //       "value": "BRKNB"
      //   })

      setSearchParams(searchParams);


      let response=Api.commonApiPost("pgr-master/report/_get",{},{tenantId:"default",reportName:metaData.reportDetails.reportName,searchParams}).then(function(response)
      {
        // console.log(response)
        setReportResult(response)
        setFlag(1);
      },function(err) {
          // console.log(err);
          alert("Select all mandatory fields or try again later");

      });

      // console.log("Show Table");

      changeButtonText("Search Again");
      // this.setState({searchBtnText:'Search Again'})

      //call api
      // setReportResult(resForm);
      showTable(true);
  }

  render() {
    let {
      searchForm,
      fieldErrors,
      isFormValid,
      isTableShow,
      handleChange,
      handleChangeNextOne,
      handleChangeNextTwo,
      buttonText,
      metaData
    } = this.props;
    let {search} = this;
    console.log(metaData);
    // console.log(searchForm);
    return (
      <div className="searchForm">
        <form onSubmit={(e) => {
          search(e)
        }}>

        <Card style={styles.marginStyle}>
          <CardHeader style={{paddingBottom:0}} title={!_.isEmpty(metaData)?metaData.reportDetails
.summary:""}/>
          <CardText style={{padding:0}}>
          <Grid>
            <Row>
              {this.handleFormFields()}
            </Row>
          </Grid>
          </CardText>
        </Card>
        <div style={{"textAlign": "center"}}>
          <br/>
            <RaisedButton type="submit" disabled={false} primary={true}  label={buttonText} />
          <br/>
          <br/>
        </div>
        </form>

      </div>
    );
  }
}

const mapStateToProps = state => ({searchForm: state.form.form, fieldErrors: state.form.fieldErrors, isFormValid: state.form.isFormValid,isTableShow:state.form.showTable,buttonText:state.form.buttonText,metaData:state.report.metaData});

const mapDispatchToProps = dispatch => ({
  setForm: (required=[],pattern=[]) => {
    console.log(required);
    dispatch({
      type: "SET_FORM",
      form:{},
      fieldErrors:{},
      isFormValid:false,
      validationData: {
        required: {
          current: [],
          required: required
        },
        pattern: {
          current: [],
          required: pattern
        }
      }
    });
  },
  initForm: () => {
    dispatch({
      type: "RESET_STATE",
      validationData: {
        required: {
          current: [],
          required: [ ]
        },
        pattern: {
          current: [],
          required: ["assessmentNo", "oldAssessmentNo", "mobileNo", "aadharNo", "doorNo"]
        }
      }
    });
  },
  handleChange: (e, property, isRequired, pattern) => {
    dispatch({type: "HANDLE_CHANGE", property, value: e.target.value, isRequired, pattern});
  },
  handleChangeNextOne: (e, property, propertyOne, isRequired, pattern) => {
    dispatch({
      type: "HANDLE_CHANGE_NEXT_ONE",
      property,
      propertyOne,
      value: e.target.value,
      isRequired,
      pattern
    })
  },
  handleChangeNextTwo: (e, property, propertyOne, propertyTwo, isRequired, pattern) => {
    dispatch({
      type: "HANDLE_CHANGE_NEXT_ONE",
      property,
      propertyOne,
      propertyTwo,
      value: e.target.value,
      isRequired,
      pattern
    })
  },
  showTable:(state)=>
  {
    dispatch({type:"SHOW_TABLE",state});
  },
  changeButtonText:(text)=>
  {
    dispatch({type:"BUTTON_TEXT",text});
  },
  toggleDailogAndSetText: (dailogState,msg) => {
    dispatch({type: "TOGGLE_DAILOG_AND_SET_TEXT", dailogState,msg});
  },
  setReportResult:(reportResult)=>{
    dispatch({type:"SET_REPORT_RESULT",reportResult})
  },
  setFlag:(flag)=>{
      dispatch({type:"SET_FLAG",flag})
  },
  setMetaData:(metaData)=>{
    dispatch({type:"SET_META_DATA",metaData})
  },
  setSearchParams:(searchParams)=>{
    dispatch({type:"SET_SEARCH_PARAMS",searchParams})
  }
});

export default connect(mapStateToProps, mapDispatchToProps)(ShowForm);


// <Card>
//     <CardHeader title={< strong style = {{color:"#5a3e1b"}} > Search Property < /strong>}/>
//
//     <CardText>
//       <Card>
//         <CardText>
//           <Grid>
//             <Row>
//               <Col xs={12} md={6}>
//                 <TextField errorText={fieldErrors.doorNo
//                   ? fieldErrors.doorNo
//                   : ""} id="doorNo" value={propertyTaxSearch.doorNo?propertyTaxSearch.doorNo:""} onChange={(e) => handleChange(e, "doorNo", false, /^([\d,/.\-]){6,10}$/g)} hintText="eg:-3233312323" floatingLabelText="Door number" />
//               </Col>
//
//               <Col xs={12} md={6}>
//                 <TextField errorText={fieldErrors.assessmentNo
//                   ? fieldErrors.assessmentNo
//                   : ""} value={propertyTaxSearch.assessmentNo?propertyTaxSearch.assessmentNo:""} onChange={(e) => handleChange(e, "assessmentNo", false, /^\d{3,15}$/g)} hintText="eg:-123456789123456" floatingLabelText="Assessment number"/>
//               </Col>
//             </Row>
//
//             <Row>
//               <Col xs={12} md={6}>
//                 <TextField errorText={fieldErrors.mobileNo
//                   ? fieldErrors.mobileNo
//                   : ""} value={propertyTaxSearch.mobileNo?propertyTaxSearch.mobileNo:""} onChange={(e) => handleChange(e, "mobileNo", false, /^\d{10}$/g)} hintText="Mobile number" floatingLabelText="Mobile number" />
//               </Col>
//
//               <Col xs={12} md={6}>
//                 <TextField errorText={fieldErrors.aadharNo
//                   ? fieldErrors.aadharNo
//                   : ""} value={propertyTaxSearch.aadharNo?propertyTaxSearch.aadharNo:""} onChange={(e) => handleChange(e, "aadharNo", false, /^\d{12}$/g)} hintText="Aadhar number " floatingLabelText="Aadhar number " />
//               </Col>
//             </Row>
//           </Grid>
//
//         </CardText>
//       </Card>
//
//       <Card>
//         <CardHeader title={< strong style = {{color:"#5a3e1b"}} > Advance Search < /strong>} actAsExpander={true} showExpandableButton={true}/>
//
//         <CardText expandable={true}>
//           <Grid>
//             <Row>
//               <Col xs={12} md={6}>
//                 <TextField errorText={fieldErrors.ownerName
//                   ? fieldErrors.ownerName
//                   : ""} value={propertyTaxSearch.ownerName?propertyTaxSearch.ownerName:""} onChange={(e) => handleChange(e, "ownerName", false, "")} hintText="Owner Name" floatingLabelText="Owner Name" />
//               </Col>
//
//               <Col xs={12} md={6}>
//                 <TextField errorText={fieldErrors.oldAssessmentNo
//                   ? fieldErrors.oldAssessmentNo
//                   : ""} value={propertyTaxSearch.oldAssessmentNo?propertyTaxSearch.oldAssessmentNo:""} onChange={(e) => handleChange(e, "oldAssessmentNo", false, /^\d{3,15}$/g)} hintText="Old Assessment Number" floatingLabelText="Old Assessment Number" />
//               </Col>
//             </Row>
//
//             <Row>
//
//               <Card>
//                 <CardHeader title={< strong style = {{color:"#5a3e1b"}} > Boundary < /strong>}/>
//
//                 <CardText>
//                   <Grid>
//                     <Row>
//                       <Col xs={12} md={6}>
//
//                         <SelectField errorText={fieldErrors.zone
//                           ? fieldErrors.zone
//                           : ""} value={propertyTaxSearch.zone?propertyTaxSearch.zone:""} onChange={(event, index, value) => {
//                             var e = {
//                               target: {
//                                 value: value
//                               }
//                             };
//                             handleChange(e, "zone", false, "")}} floatingLabelText="Zone	Drop " >
//                           <MenuItem value={1} primaryText="Never"/>
//                           <MenuItem value={2} primaryText="Every Night"/>
//                           <MenuItem value={3} primaryText="Weeknights"/>
//                           <MenuItem value={4} primaryText="Weekends"/>
//                           <MenuItem value={5} primaryText="Weekly"/>
//                         </SelectField>
//
//                       </Col>
//
//                       <Col xs={12} md={6}>
//                         <SelectField errorText={fieldErrors.ward
//                           ? fieldErrors.ward
//                           : ""} value={propertyTaxSearch.ward?propertyTaxSearch.ward:""} onChange={(event, index, value) =>{
//                             var e = {
//                               target: {
//                                 value: value
//                               }
//                             };
//                             handleChange(e, "ward", false, "")}
//                           } floatingLabelText="Ward" >
//                           <MenuItem value={1} primaryText="Never"/>
//                           <MenuItem value={2} primaryText="Every Night"/>
//                           <MenuItem value={3} primaryText="Weeknights"/>
//                           <MenuItem value={4} primaryText="Weekends"/>
//                           <MenuItem value={5} primaryText="Weekly"/>
//                         </SelectField>
//                       </Col>
//                     </Row>
//
//                     <Row>
//                       <Col xs={12} md={6}>
//                         <SelectField errorText={fieldErrors.location
//                           ? fieldErrors.location
//                           : ""} value={propertyTaxSearch.location?propertyTaxSearch.location:""} onChange={(event, index, value) => {
//                             var e = {
//                               target: {
//                                 value: value
//                               }
//                             };
//                             handleChange(e, "location", false, "")}} floatingLabelText="Location" >
//                           <MenuItem value={1} primaryText="Never"/>
//                           <MenuItem value={2} primaryText="Every Night"/>
//                           <MenuItem value={3} primaryText="Weeknights"/>
//                           <MenuItem value={4} primaryText="Weekends"/>
//                           <MenuItem value={5} primaryText="Weekly"/>
//                         </SelectField>
//                       </Col>
//
//                     </Row>
//                   </Grid>
//
//                 </CardText>
//               </Card>
//
//             </Row>
//
//             <Row>
//               <Card>
//                 <CardHeader title={< strong style = {{color:"#5a3e1b"}} > Search Property by Demand < /strong>}/>
//                 <CardText>
//                   <Grid>
//                     <Row>
//                       <Col xs={12} md={6}>
//                       <TextField errorText={fieldErrors.demandFrom
//                         ? fieldErrors.demandFrom
//                         : ""} value={propertyTaxSearch.demandFrom?propertyTaxSearch.demandFrom:""} onChange={(e) => handleChange(e, "demandFrom", false, /^\d$/g)} hintText="Demand From" floatingLabelText="Demand From" />
//
//
//                       </Col>
//
//                       <Col xs={12} md={6}>
//                       <TextField errorText={fieldErrors.demandTo
//                         ? fieldErrors.demandTo
//                         : ""} value={propertyTaxSearch.demandTo?propertyTaxSearch.demandTo:""} onChange={(e) => handleChange(e, "demandTo", false, /^\d$/g)} hintText="Demand To" floatingLabelText="Demand To" />
//
//
//                       </Col>
//                     </Row>
//                   </Grid>
//                 </CardText>
//               </Card>
//             </Row>
//
//           </Grid>
//         </CardText>
//       </Card>
//       <div style={{
//         float: "center"
//       }}>
//         <RaisedButton type="submit" disabled={!isFormValid} label={buttonText} backgroundColor={"#5a3e1b"} labelColor={white}/>
//         <RaisedButton label="Close"/>
//       </div>
//     </CardText>
//   </Card>
